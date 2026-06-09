package ag.com.dbo.services.management;

import ag.com.dbo.controllers.FullEtlInstance;
import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.models.management.*;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;

import ag.com.dbo.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ag.com.dbo.services.queue.utils.VarSupport.*;
import static ag.com.dbo.utils.Utils.saveError;

@Slf4j
@Service
public class EngineService {
    private final EtlRepository etlRepository;
    private final EtlInstanceRepository etlInstanceRepository;
    private final StepRepository stepRepository;
    private final StepInstanceRepository stepInstanceRepository;
    private final RestClient restClient;

    @Value("${queue.enqueue.path}")
    private String enqueuePath;


    public EngineService(EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
                         StepRepository stepRepository, StepInstanceRepository stepInstanceRepository, RestClient restClient

    ) {
        this.etlRepository = etlRepository;
        this.etlInstanceRepository = etlInstanceRepository;
        this.stepRepository = stepRepository;
        this.stepInstanceRepository = stepInstanceRepository;
        this.restClient = restClient;
    }


//    @Scheduled(fixedRateString = "${scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
    public void schedule(){
        log.info("sh!");
        List<Etl> started = etlRepository.findByStatus(1);
        log.info("get:"+started);
        for (Etl etl : started){
            createEtlInstance(etl);
        }
    }
//Start !!!
    public void createEtlInstance(Etl etl){
        log.info("get one:{}", etl.getId());
        EtlInstance ei = new EtlInstance();
        ei.setEtl(etl);
        ei.setStart(OffsetDateTime.now());
        ei.setStatus(EtlStatus.Started.name());
        ei.setComment(etl.getComment());
        etlInstanceRepository.saveAndFlush(ei);
        log.debug("save  etl status:{}", ei);
        createStepInstances(ei);
    }

    private void createStepInstances(EtlInstance etl){
        log.info("steps for etl instance: {}", etl.getEtlInstanceId());
        List<Step> steps= stepRepository.findAllStepsByEtl(etl.getEtl().getId());
        log.info("steps1: {}", steps.stream().map(Step::getStepId));

        List<StepInstance> sis = new ArrayList<>(steps.size());
        try {
            for (Step step : steps) {
                StepInstance si = new StepInstance();
                si.setStepInstanceId(UUID.randomUUID().toString());
                si.setEtl(etl.getEtl());
                si.setStep(step);
                si.setEtlInstance(etl);
                si.setVars(step.getVars());
                si.setActive(step.getStepActive());
                si.setMaxAttempts(step.getMaxAttempts());
                si.setName(step.getName());
                si.setSaveCalculate(step.getSaveCalculate());
                si.setGroovyScript(step.getGroovyScript());
                etl.setEtlVars(merge(etl.getEtlVars(), si.getVars(),si.getName()));
                log.debug("si:{} ", si);
                sis.add(si);
            }
        }catch(Exception e){
            etl.addLog("Cannot build vars field: "+e.getMessage());
            etl.setStatus(EtlStatus.Fail.name());
            etlInstanceRepository.saveAndFlush(etl);
            return;
        }
        log.info("steps sis:"+sis);
        List<StepInstance> sisOut = stepInstanceRepository.saveAllAndFlush(sis);
        etl = etlInstanceRepository.saveAndFlush(etl);

        log.info("saved new etl status: {}", sisOut);
        // stepId-> stepInstanceId old -> new
        Map<BigInteger,String> stepInstanceRelation = sisOut.stream().collect(
                Collectors.toMap( x -> x.getStep().getStepId(), StepInstance::getStepInstanceId )
        );
        // Step instances // set parents
        for (StepInstance si :sisOut){
            // Steps
            BigInteger[] parentStep = si.getStep().getParentStepIds();
            if (parentStep != null) {
                String[] parentsSi = Arrays.stream(parentStep).map(stepInstanceRelation::get).toArray(String[]::new);
                si.setParentStepInstanceIds(parentsSi);
            }
           StepInstance sisOute = stepInstanceRepository.saveAndFlush(si);
           log.info("saved updated etl status: {}", sisOute);
        }
        try {
            makeStep(etl);
        } catch (Exception e){
            etl.addLog("cannot calculate logic: " + e.getMessage());
            etl.setStatus(StepStatus.Failed.name());
            return;
        }
    }
// step finished and need new step
    public void stepFrom(String stepInstanceId ){
        StepInstance si = stepInstanceRepository.getReferenceById(stepInstanceId);
        FullEtlInstance fullEtlInstance = null;
        try {
            fullEtlInstance = getFullEtlInstance(si.getEtlInstance());
        }catch(Exception e ){
            si.addLog("cannot calculate logic:"+e.getMessage());
            si.setStatus(StepStatus.Failed.name());
            return;
        }
        if (checkAllStepInstances(si, fullEtlInstance)){
            log.info("!!!!!ETL finished !!!!!!");
            EtlInstance ei =si.getEtlInstance();
            ei.setStop(OffsetDateTime.now());
            if (getEtlInstanceErrorExists(si, fullEtlInstance)){
                ei.setStatus(EtlStatus.Fail.name());
            }else{
                ei.setStatus(EtlStatus.Success.name());
            }
            etlInstanceRepository.saveAndFlush(ei);
            return;
        }
        Set<String> children = fullEtlInstance.getParentToChildrenStep().get(stepInstanceId);
        List<String> incorrectWayId = null;
        // check branch !!!
        // TODO
        if(StringUtils.isNotEmpty(si.getStep().getBranchCondition())){
            try {

                List<String> correctWsyNames =execGroovyScript(si);
                List<String> correctWayId = fullEtlInstance.getCorrectWayInIds(correctWsyNames, stepInstanceId);
                incorrectWayId = fullEtlInstance.getIncorrectWayIds(correctWsyNames, stepInstanceId);
                if (CollectionUtils.isEmpty(correctWayId)){
                    log.info("last step!:{}",stepInstanceId);
                    children= Collections.emptySet();
                }else{
                    children = new  HashSet<>(correctWayId);
                }
            }catch (Exception e){
                saveError(si, stepInstanceRepository,e, "Wrong Groovy script");
            }
        }
        if (CollectionUtils.isEmpty(children) ){
            log.info("last step!:{}",stepInstanceId);
        }else{
            FullEtlInstance finalFullEtlInstance = fullEtlInstance;

            ///  !!!!
            children.stream()
                    .map(x-> finalFullEtlInstance.getSiBase().get(x))
                    .forEach(stepInstance-> {
                        log.info("try to run child:{}", stepInstance.getStepInstanceId());
                        enqueueTask(stepInstance, finalFullEtlInstance);
                    });
        }
        if(!CollectionUtils.isEmpty(incorrectWayId)){
            // set all branch status to StepStatus.Missed
            List<StepInstance> listSii= new ArrayList<>(incorrectWayId.size());
            List<StepInstance> result= new ArrayList<>();
            for(String id: incorrectWayId){
                result.addAll(recursiveSetMissed(fullEtlInstance, id, false));
            }
            stepInstanceRepository.saveAllAndFlush(result);


        }
    }

    /**
     * should be next
     * @param fullEtlInstance
     * @param stepInstanceId
     * @return
     */


    private List<StepInstance> recursiveSetMissed(FullEtlInstance fullEtlInstance, String stepInstanceId, boolean recursive){
        List<StepInstance> result = new ArrayList<>();
        StepInstance  sii = fullEtlInstance.getSiBase().get(stepInstanceId);
        if (!recursive) {
            sii.setStatus(StepStatus.Missed.name());
            result.add(sii);
        }else{
            String[] parents = sii.getParentStepInstanceIds();
            if (ArrayUtils.isNotEmpty(parents) && parents.length==1){
                sii.setStatus(StepStatus.Missed.name());
                result.add(sii);
            }
        }
        Set<String> children = fullEtlInstance.getParentToChildrenStep().get(stepInstanceId);
        if (children!=null){
            children.forEach(x-> result.addAll(recursiveSetMissed(fullEtlInstance,x, true)));
        }
        return result;
    }

    private boolean checkAllStepInstances(StepInstance si, FullEtlInstance fullEtlInstance){
        List<StepInstance> steps = fullEtlInstance.getSteps();
        long finishedCount= steps.stream().filter(x->
                (StepStatus.Success.name().equals(x.getStatus()) ||
                        StepStatus.Missed.name().equals(x.getStatus()) ||
                        StepStatus.Failed.name().equals(x.getStatus()))

        ).count();
        return  (steps.size() == finishedCount);

    }
    private boolean getEtlInstanceErrorExists(StepInstance si, FullEtlInstance fullEtlInstance){
        List<StepInstance> steps = fullEtlInstance.getSteps();
        long errorCount= steps.stream()
                .filter(x-> StepStatus.Failed.name().equals(x.getStatus()))
                .count();
        return  (errorCount>0);

    }


    private void makeStep(EtlInstance etlInstance) throws Exception {

        log.info("make first step etlInstance id:"+etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        FullEtlInstance fullEtlInstance = getFullEtlInstance(etlInstance);
        // root steps can be with condition ? I suppose not.
        List<StepInstance> startSteps = fullEtlInstance.getSteps().stream().filter(x-> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
        for (StepInstance si : startSteps){
            enqueueTask(si, fullEtlInstance);
        }
    }

    private  FullEtlInstance getFullEtlInstance(EtlInstance etlInstance) throws Exception {
        log.info("make step etlInstance id:"+etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        // map Si.id-> Si for one etl instance (cache)
        Map<String, StepInstance> stepInstancesEltInstance =   steps.stream().collect(
                Collectors.toMap(StepInstance::getStepInstanceId, Function.identity())
        );

        // make a map: parens Step instance to list of children
        Map<String, Set<String>> parentToChildrenStep= new HashMap<>();
        for(StepInstance si : steps) {
            if (ArrayUtils.isNotEmpty(si.getParentStepInstanceIds())){
                for (String parentId : si.getParentStepInstanceIds()) {
                    Set<String> children = parentToChildrenStep.get(parentId);
                    if (children == null) {
                        children = new HashSet<>(Collections.singletonList(si.getStepInstanceId()));
                        parentToChildrenStep.put(parentId, children);
                    } else {
                        children.add(si.getStepInstanceId());
                    }
                }
            }
        }

        log.debug("list tree:{}", stepInstancesEltInstance);
        //
        List<String> finishSteps = stepInstancesEltInstance.keySet().stream().filter( k-> !parentToChildrenStep.containsKey(k)).toList();
        log.debug("finishSteps: {}", finishSteps);
        // try to run recursive from root

        return  new FullEtlInstance(parentToChildrenStep, stepInstancesEltInstance, finishSteps, steps);

    }
    /**
     * For branch calculating
     * @param si
     */

    private List<String> execGroovyScript(StepInstance si ) throws JsonProcessingException {
        String sVars = si.getEtlInstance().getEtlVars();
        if (StringUtils.isNotEmpty(sVars)) {
            Map<String, Object> vars = stringToJsonVar(sVars);
            Binding binding = new Binding();
            binding.setVariable("stepName", si.getName());
            binding.setVariable("vars", vars);
            GroovyShell shell = new GroovyShell(binding);
            Object oResult = shell.evaluate(si.getStep().getBranchCondition());
            return stringBranchVars(oResult);

        }
        return null;
    }

    /**
     *   need full check because maybe next step
     * @param currentSi
     * @param fullEtlInstance
     */
    private void enqueueTask( StepInstance currentSi, FullEtlInstance fullEtlInstance ){
        log.info("Step: {}", currentSi.getStepInstanceId());
        if (currentSi.getStatus() == null) { // not started yet
            // if all parents Success or Missed or Failed
            String[] parents = currentSi.getParentStepInstanceIds();
            List<StepInstance> parentOkSi = Collections.emptyList();
            if (parents != null) {
                parentOkSi = Arrays.stream(parents)
                        .map(x -> fullEtlInstance.getSiBase().get(x))
                        .filter(x -> StepStatus.Success.name().equals(x.getStatus()) ||
                                StepStatus.Missed.name().equals(x.getStatus()) ||
                                StepStatus.Failed.name().equals(x.getStatus()) )
                        .toList();
                if (ArrayUtils.isEmpty(parents) || parents.length == parentOkSi.size()){
                    startStepInstance(currentSi);
                }

            }
        }
    }


    /**
     * Strart StepInstance and return
     * @param si
     * @return
     */
    private boolean startStepInstance(StepInstance si){
        log.info("-------!!!!!!startStep: {}",si);
        if (si.getStep().getStepActive()) {
            try {
                si.setStatus(StepStatus.InProcess.name());
                stepInstanceRepository.saveAndFlush(si);
                sendToQueue(si);
                log.info("sent to queue:{}", si.getStepInstanceId());
                return true;
            }catch (Throwable e){
                saveError(si, stepInstanceRepository,e,"cannot send to queue");
                return true;
            }
        }else{
            log.info("startStep:{} inactive",si);
            si.setStatus(StepStatus.Missed.name());
            si.setStart(OffsetDateTime.now());
            si.setStop(OffsetDateTime.now());
            si.addLog("Deactivated by status.");
            stepInstanceRepository.saveAndFlush(si);
            return true;
        }
    }


    private void sendToQueue(StepInstance si) throws JsonProcessingException {
        log.info("sendToQueue:{}", si);
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(si.getStepInstanceId());
        taskRequest.setCommandProfile(si.getStep().getDataLoading().getProps());
        taskRequest.setCalculateType(si.getStep().getDataLoading().getName());
        taskRequest.setMaxAttempts(si.getStep().getMaxAttempts());
        taskRequest.setParameters(si.getStep().getVars());
        taskRequest.setSaveCalculate(si.getSaveCalculate());
        taskRequest.setGroovyScript(si.getGroovyScript());
        try {
            this.restClient.put().uri(enqueuePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(taskRequest)
                    .retrieve()
                    .toBodilessEntity();
        }catch(Throwable e){
            saveError(si, stepInstanceRepository,  e, "cannot send to queue");

        }
    }
}
