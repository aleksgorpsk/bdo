package ag.com.dbo.services;

import ag.com.dbo.controllers.FullEtlInstance;
import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.models.management.*;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;

import ag.com.dbo.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public EtlInstance createEtlInstance(Etl etl){
        log.info("get one:"+etl);
        EtlInstance ei = new EtlInstance();
        ei.setEtl(etl);
        ei.setStart(OffsetDateTime.now());
        ei.setStatus(EtlStatus.Started.name());
        ei.setComment(etl.getComment());
        etlInstanceRepository.saveAndFlush(ei);
        log.info("save  etl status:"+ei);
        createStepInstances(ei);
        return ei;
    }

    private void createStepInstances(EtlInstance etl){
        log.info("steps:"+etl);
        List<Step> steps= stepRepository.findAllStepsByEtl(etl.getEtl().getId());
        log.info("steps1:"+steps);

        List<StepInstance> sis = new ArrayList<>(steps.size());
        for(Step step: steps){
            StepInstance si = new StepInstance();
            si.setStepInstanceId(UUID.randomUUID().toString());
            si.setEtl(etl.getEtl());
            si.setStep(step);
            si.setEtlInstance(etl);
            si.setVars(step.getVars());
            si.setActive(step.getStepActive());
            si.setMaxAttempts(step.getMaxAttempts());
            log.info("si:"+si);
            sis.add(si);
        }
        log.info("steps sis:"+sis);
        List<StepInstance> sisout = stepInstanceRepository.saveAllAndFlush(sis);

        log.info("saved new etl status:"+sisout);
// stepId-> stepInstanceId old-> new
        Map<BigInteger,String> stepInstanceRelation= new HashMap<>(sisout.size());
        for (StepInstance si :sisout){
            stepInstanceRelation.put(si.getStep().getStepId(), si.getStepInstanceId());
        }
        // Step instances // set parents
        for (StepInstance si :sisout){
            // Steps
            BigInteger[] parentStep = si.getStep().getParentStepIds();
            if (parentStep != null) {
                String[] parentsSi = Arrays.stream(parentStep).map(stepInstanceRelation::get).toArray(String[]::new);
                si.setParentStepInstanceIds(parentsSi);
            }
            StepInstance sisoute = stepInstanceRepository.saveAndFlush(si);
            log.info("saved updated etl status:"+sisoute);
        }
        log.info("2:"+stepInstanceRelation);
        makeStep(etl);
    }
// step finished need new step
    public void stepFrom(String stepInstanceId ){
        StepInstance si = stepInstanceRepository.getReferenceById(stepInstanceId);
        FullEtlInstance fullEtlInstance=getFullEtlInstance(si.getEtlInstance());

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
        if (CollectionUtils.isEmpty(children) ){
            log.info("last step!:{}",stepInstanceId);
        }else{
            children.stream()
                    .map(x->fullEtlInstance.getSiBase().get(x))
                    .forEach(y-> {
                        log.info("try to run child:{}", y);
                        enqueueTask(y, fullEtlInstance);
                    });
        }
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


    private void makeStep(EtlInstance etlInstance){

        log.info("make first step etlInstance id:"+etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        FullEtlInstance fullEtlInstance = getFullEtlInstance(etlInstance);

        List<StepInstance> startSteps = fullEtlInstance.getSteps().stream().filter(x-> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
        for (StepInstance si : startSteps){
            enqueueTask(si, fullEtlInstance);
        }
    }

    private  FullEtlInstance getFullEtlInstance(EtlInstance etlInstance){
        log.info("make step etlInstance id:"+etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        // map Si.id-> Si for one etl instance
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

        log.info("list tree:{}", stepInstancesEltInstance);
        List<String> finishSteps = stepInstancesEltInstance.keySet().stream().filter( k-> !parentToChildrenStep.containsKey(k)).toList();
        log.info("finishSteps: {}", finishSteps);
        // try to run recursive from root

        return  new FullEtlInstance(parentToChildrenStep, stepInstancesEltInstance, finishSteps, steps);

    }
    /**
     *
     * @param currentSi
     */

    private void enqueueTask( StepInstance currentSi, FullEtlInstance fullEtlInstance ){
        log.info("Step: {}", currentSi);
        if (currentSi.getStatus()==null) { // not started yet
            // if all parents Success or Missed
            String[] parents = currentSi.getParentStepInstanceIds();
            List<StepInstance> parentOkSi = Collections.emptyList();
            if (parents != null) {
                parentOkSi = Arrays.stream(parents)
                        .map(x -> fullEtlInstance.getSiBase().get(x))
                        .filter(x -> StepStatus.Success.name().equals(x.getStatus()) ||
                                StepStatus.Missed.name().equals(x.getStatus()))
                        .toList();
            }
            if (ArrayUtils.isEmpty(parents) || parents.length == parentOkSi.size()){
                startStepInstance(currentSi);
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
                si.setStatus(StepStatus.ReadyToQueue.name());
                stepInstanceRepository.saveAndFlush(si);
                sendToQueue(si);
                log.info("sent toqueue:{}", si.getStepInstanceId());
                return true;
            }catch (Throwable e){
                String error = OffsetDateTime.now()+ ": cannot send to queue: "+e.getMessage();
                String log = (si.getLog()==null) ? error: si.getLog()+ System.lineSeparator()+ error;
                return true;
            }
        }else{
            log.info("startStep:{} inactive",si);
            si.setStatus(StepStatus.Missed.name());
            si.setStart(OffsetDateTime.now());
            si.setStop(OffsetDateTime.now());
            si.setLog("deactivated");
            stepInstanceRepository.saveAndFlush(si);
            return true;
        }
    }


    private void sendToQueue(StepInstance si) throws JsonProcessingException {

        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(si.getStepInstanceId());
        taskRequest.setCommandProfile(si.getStep().getDataLoading().getProps());
        taskRequest.setCalculateType(si.getStep().getCalculateMethod());
        taskRequest.setMaxAttempts(si.getStep().getMaxAttempts());
        taskRequest.setParameters(Utils.getMap(si.getStep().getVars()));
        try {
            this.restClient.put().uri(enqueuePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(taskRequest)
                    .retrieve()
                    .toBodilessEntity();
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
