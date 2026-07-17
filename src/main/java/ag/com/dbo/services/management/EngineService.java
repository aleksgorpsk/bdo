package ag.com.dbo.services.management;

import ag.com.dbo.controllers.FullEtlInstance;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.models.checker.SensorModel;
import ag.com.dbo.models.management.*;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.models.script.ScriptType;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;

import ag.com.dbo.services.Utils;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.ListUtils;


import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ag.com.dbo.services.Utils.getSensor;
import static ag.com.dbo.services.queue.utils.VarSupport.*;
import static ag.com.dbo.services.queue.utils.VarSupport.stringToJsonVar;
import static ag.com.dbo.utils.Utils.saveError;

@Slf4j
@Service
public class EngineService {
    private final EtlRepository etlRepository;
    private final EtlInstanceRepository etlInstanceRepository;
    private final StepRepository stepRepository;
    private final StepInstanceRepository stepInstanceRepository;
    private final ScriptService scriptService;
    private final ExternalService externalService;

    public EngineService(
            EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
            StepRepository stepRepository, StepInstanceRepository stepInstanceRepository,
            ScriptService scriptService, ExternalService externalService
    ) {
        this.etlRepository = etlRepository;
        this.etlInstanceRepository = etlInstanceRepository;
        this.stepRepository = stepRepository;
        this.stepInstanceRepository = stepInstanceRepository;
        this.scriptService = scriptService;
        this.externalService = externalService;
    }


    //    @Scheduled(fixedRateString = "${scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
    public void schedule() {
        log.info("sh!");
        List<Etl> started = etlRepository.findByStatus(1);
        log.info("get:" + started);
        for (Etl etl : started) {
            startEtl(etl);
        }
    }

    public String addDate(EtlInstance ei){
        String startEtl="{ \"startEtl\": \""+OffsetDateTime.now().toString()+"\"}";
        try {
          return  merge(ei.getEtlVars(),startEtl);
        } catch (JsonProcessingException e) {
            ei.addLog("Error: "+e.getMessage());
        }
        return null;
    }
    //Start !!!
    public void startEtl(Etl etl) {
        log.info("get one:{}", etl.getId());
        EtlInstance ei = new EtlInstance();
        ei.setEtl(etl);
        ei.setStart(OffsetDateTime.now());
        ei.setStatus(EtlStatus.InProgress.name());
        ei.setComment(etl.getComment());
        ei.setEtlVars(addDate(ei));
        etlInstanceRepository.saveAndFlush(ei);
        log.debug("save  etl status:{}", ei);
        createStepInstances(ei);
    }

    public void createStepInstances(EtlInstance etl) {
        log.info("steps for etl instance: {}", etl.getEtlInstanceId());
        List<Step> steps = stepRepository.findAllStepsByEtl(etl.getEtl().getId());
        log.info("steps1: {}", steps.stream().map(Step::getStepId));

        List<StepInstance> sis = new ArrayList<>(steps.size());
        try {
            for (Step step : steps) {
                StepInstance si = new StepInstance();
                si.setStepInstanceId(UUID.randomUUID().toString());
                si.setEtl(etl.getEtl());
                si.setStep(step);
                si.setStart(OffsetDateTime.now());
                si.setEtlInstance(etl);
                si.setVars(step.getVars());
                si.setActive(step.getStepActive());
                si.setMaxAttempts(step.getMaxAttempts());
                si.setName(step.getName());
                si.setScript(step.getScript());
                etl.setEtlVars(merge(etl.getEtlVars(), si.getVars(), si.getName()));
                si.setStepType(step.getStepType());
                si.setNextTest(null);
                si.setTags(step.getTags());
                log.debug("si:{} ", si);
                sis.add(si);
            }
        } catch (Exception e) {
            etl.addLog("Cannot build vars field: " + e.getMessage());
            etl.setStatus(EtlStatus.Fail.name());
            etlInstanceRepository.saveAndFlush(etl);
            return;
        }
        log.info("steps sis:" + sis);
        List<StepInstance> sisOut = stepInstanceRepository.saveAllAndFlush(sis);
        etl = etlInstanceRepository.saveAndFlush(etl);

        log.info("saved new etl status: {}", sisOut);
        // stepId-> stepInstanceId old -> new
        Map<BigInteger, String> stepInstanceRelation = sisOut.stream().collect(
                Collectors.toMap(x -> x.getStep().getStepId(), StepInstance::getStepInstanceId)
        );
        // Step instances // set parents
        for (StepInstance si : sisOut) {
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
        } catch (Exception e) {
            etl.addLog("cannot calculate logic: " + e.getMessage());
            etl.setStatus(StepStatus.Failed.name());
            return;
        }
    }

    /*
    public void checkSensor(StepInstance si, FullEtlInstance fullEtlInstance)  {
        if (StepStatus.InWait.name().equals(si.getStatus())
                && StepType.Sensor.name().equals(si.getStepType())){

            try {
                SensorModel sModel = externalStepTypeService.getSensor(si.getVars());

        if(si.getNextTest()==null){
            si.setNextTest(OffsetDateTime.now().plusSeconds(sModel.getAttemptTimeOut()));
        }else{
            si.setNextTest(si.getNextTest().plusSeconds(sModel.getAttemptTimeOut()));
        }
        stepInstanceRepository.saveAndFlush(si);

        if(fullEtlInstance==null) {
            try {
                fullEtlInstance = getFullEtlInstance(si.getEtlInstance());
            } catch (Exception e) {
                si.addLog("cannot calculate logic:" + e.getMessage());
                si.setStatus(StepStatus.Failed.name());
                return;
            }
        }




                boolean result = externalStepTypeService.checkSensorScript(si);
                log.info("!!!!!!!:{}",result);

            }catch (Exception e){
                saveError(si,stepInstanceRepository,e,"Cannot parse Sensor script");
            }
            //TODO!!!
        }
    }

    */
    public void stepFrom(String stepInstanceId) {
        StepInstance si = stepInstanceRepository.getReferenceById(stepInstanceId);
        stepFrom(si);
    }



    /**
     * step finished and need new step
     * @param si
     */
    public void stepFrom(StepInstance si) {
        FullEtlInstance fullEtlInstance = null;
        si.addLog("Continue process Step. Second part.");

        try {
            fullEtlInstance = getFullEtlInstance(si.getEtlInstance());
        } catch (Exception e) {
            si.addLog("cannot calculate logic:" + e.getMessage());
            si.setStatus(StepStatus.Failed.name());
            stepInstanceRepository.saveAndFlush(si);
            return;
        }
        //   run script
        stepInstanceRepository.saveAndFlush(si);
        ScriptDefinition[] scripts= scriptService.getAppropriateScript(si, ScriptType.Common);
        if(scripts.length>0) {
            ScriptResponse scriptResponse = scriptService.runScript(si, scripts);
            if (!Constants.OK.equals(scriptResponse.getStatus())){
                log.error("Error send Script:" + scriptResponse.getResponse());
                si.setStatus(StepStatus.Failed.name());
                stepInstanceRepository.saveAndFlush(si);
                return;
            }
            try {
                saveResult(si, scriptResponse);
            }catch (JsonProcessingException e){
                si.addLog("Cannot parse response: "+e.getMessage());
            }
            si.addLog("Process script(s): "+scripts);
            stepInstanceRepository.saveAndFlush(si);
        }

      // check finish
        EtlInstance ei = si.getEtlInstance();
        if (StepStatus.Success.name().equals(si.getStatus())) {
            if (checkAllStepInstances(si, fullEtlInstance)) {
                log.info("!!!!!ETL finished !!!!!!");
                ei.setStop(OffsetDateTime.now());
                if (getEtlInstanceErrorExists(si, fullEtlInstance)) {
                    ei.setStatus(EtlStatus.Fail.name());
                } else {
                    ei.setStatus(EtlStatus.Success.name());
                }
                etlInstanceRepository.saveAndFlush(ei);
                return;
            }
        } else if (StepStatus.Failed.name().equals(si.getStatus())) {
            si.setStatus(StepStatus.Failed.name());
            ei.setStatus(EtlStatus.Fail.name());
            stepInstanceRepository.saveAndFlush(si);
            etlInstanceRepository.saveAndFlush(ei);
        }


        // check sensor

        if (StepStatus.InWait.name().equals(si.getStatus())
                && StepType.Sensor.name().equals(si.getStepType())) {
            try {
                SensorModel sModel =  getSensor(si.getVars());
                if (si.getNextTest() == null) {
                    si.setNextTest(si.getStart().plusSeconds(sModel.getAttemptTimeOut()));
                } else {
                    si.setNextTest(si.getNextTest().plusSeconds(sModel.getAttemptTimeOut()));
                }
                stepInstanceRepository.saveAndFlush(si);
            } catch (JsonProcessingException e) {
                saveError(si, stepInstanceRepository, e, "Cannot parse Sensor parameters");
                return;

            } catch (Exception e) {
                saveError(si, stepInstanceRepository, e, " Sensor Error");
                return;
            }
        }
        // TODO BRANCH

        Set<String> children = fullEtlInstance.getParentToChildrenStep().get(si.getStepInstanceId());
        List<String> incorrectWayId = null;

        if (Utils.isContainsStepType(si,StepType.Branch)) {
            try {
                List<String> correctWsyNames = getBranches(si);
                List<String> correctWayId = fullEtlInstance.getCorrectWayInIds(correctWsyNames, si.getStepInstanceId());
                incorrectWayId = fullEtlInstance.getIncorrectWayIds(correctWsyNames, si.getStepInstanceId());

                if (CollectionUtils.isEmpty(correctWayId)) {
                    log.info("last step!:{}", si.getStepInstanceId());
                    children = Collections.emptySet();
                } else {
                    children = new HashSet<>(correctWayId);
                }
            } catch (Exception e) {

                saveError(si, stepInstanceRepository, e, "Wrong Groovy script");
            }
        }

        si.setStatus(StepStatus.Success.name());
        si.setStop(OffsetDateTime.now());
        stepInstanceRepository.saveAndFlush(si);

        if (CollectionUtils.isEmpty(children)) { // if no children
            log.info("last step!:{}", si.getStepInstanceId());
        } else {
            FullEtlInstance finalFullEtlInstance = fullEtlInstance;

            List<StepInstance> siList = children.stream()
                    .map(x -> finalFullEtlInstance.getSiBase().get(x))
                    .filter(StepInstance::getActive).toList();
                    si.addLog("Run to queue :"+
                            String.join(",",siList.stream().map(x->x.getName()).toList() ));
                    for(StepInstance siCh :siList){
                        enqueueTask(siCh, finalFullEtlInstance);
                    }

        }

        if (!CollectionUtils.isEmpty(incorrectWayId)) {
            // set all branch status to StepStatus.Missed
            List<StepInstance> listSii = new ArrayList<>(incorrectWayId.size());
            List<StepInstance> result = new ArrayList<>();
            for (String id : incorrectWayId) {
                result.addAll(recursiveSetMissed(fullEtlInstance, id, false));
            }
            stepInstanceRepository.saveAllAndFlush(result);
        }
    }

    private List<String> getBranches(StepInstance si) throws Exception {
        ScriptDefinition[] scripts= scriptService.getAppropriateScript(si, ScriptType.Branch);
        if(scripts.length>0) {
            try {
                ScriptResponse scriptResponse = scriptService.runScript(si, scripts);
                if (!Constants.OK.equals(scriptResponse.getStatus())) {
                    log.error("Error send Script:" + scriptResponse.getResponse());
                    si.setStatus(StepStatus.Failed.name());
                    stepInstanceRepository.saveAndFlush(si);
                    return Collections.EMPTY_LIST;
                }
                try {
                    saveResult(si, scriptResponse);
                } catch (JsonProcessingException e) {
                    si.addLog("Cannot parse response: " + e.getMessage());
                }
                si.addLog("Process script(s): " + scripts);
                stepInstanceRepository.saveAndFlush(si);
                return stringToObject(scriptResponse.getResponse(), List.class);
            }catch(Exception x){
                saveError(si, stepInstanceRepository, x, "Error in :"+scripts);
                return Collections.EMPTY_LIST;
            }
        }else{
            si.addLog("No script for step: "+si.getName());
        }
        return Collections.EMPTY_LIST;
    }
    /**
     * should be next
     *
     * @param fullEtlInstance
     * @param stepInstanceId
     * @return List<StepInstance>
     */


    private List<StepInstance> recursiveSetMissed(FullEtlInstance fullEtlInstance, String stepInstanceId, boolean recursive) {
        List<StepInstance> result = new ArrayList<>();
        StepInstance sii = fullEtlInstance.getSiBase().get(stepInstanceId);
        if (!recursive) {
            sii.setStatus(StepStatus.Missed.name());
            result.add(sii);
        } else {
            String[] parents = sii.getParentStepInstanceIds();
            if (ArrayUtils.isNotEmpty(parents) && parents.length == 1) {
                sii.setStatus(StepStatus.Missed.name());
                result.add(sii);
            }
        }
        Set<String> children = fullEtlInstance.getParentToChildrenStep().get(stepInstanceId);
        if (children != null) {
            children.forEach(x -> result.addAll(recursiveSetMissed(fullEtlInstance, x, true)));
        }
        return result;
    }

    private boolean checkAllStepInstances(StepInstance si, FullEtlInstance fullEtlInstance) {
        List<StepInstance> steps = fullEtlInstance.getSteps();
        long finishedCount = steps.stream().filter(x ->
                (StepStatus.Success.name().equals(x.getStatus()) ||
                        StepStatus.Missed.name().equals(x.getStatus()) ||
                        StepStatus.Failed.name().equals(x.getStatus()))

        ).count();
        return (steps.size() == finishedCount);

    }

    private boolean getEtlInstanceErrorExists(StepInstance si, FullEtlInstance fullEtlInstance) {
        List<StepInstance> steps = fullEtlInstance.getSteps();
        long errorCount = steps.stream()
                .filter(x -> StepStatus.Failed.name().equals(x.getStatus()))
                .count();
        return (errorCount > 0);

    }


    private void makeStep(EtlInstance etlInstance) throws Exception {

        log.info("make first step etlInstance id:" + etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps = stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        FullEtlInstance fullEtlInstance = getFullEtlInstance(etlInstance);
        // root steps can be with condition ? I suppose not.
        List<StepInstance> startSteps = fullEtlInstance.getSteps().stream().filter(x -> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
        for (StepInstance si : startSteps) {
            enqueueTask(si, fullEtlInstance);
        }
    }

    private FullEtlInstance getFullEtlInstance(EtlInstance etlInstance) throws Exception {
        log.info("make step etlInstance id: {}", etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps = stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        // map Si.id-> Si for one etl instance (cache)
        Map<String, StepInstance> stepInstancesEltInstance = steps.stream().collect(
                Collectors.toMap(StepInstance::getStepInstanceId, Function.identity())
        );

        // make a map: parens Step instance to list of children
        Map<String, Set<String>> parentToChildrenStep = new HashMap<>();
        for (StepInstance si : steps) {
            if (ArrayUtils.isNotEmpty(si.getParentStepInstanceIds())) {
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
        List<String> finishSteps = stepInstancesEltInstance.keySet().stream().filter(k -> !parentToChildrenStep.containsKey(k)).toList();
        log.debug("finishSteps: {}", finishSteps);
        // try to run recursive from root

        return new FullEtlInstance(parentToChildrenStep, stepInstancesEltInstance, finishSteps, steps);

    }


    /**
     * need full check because maybe not all parents is ready
     *
     * @param currentSi
     * @param fullEtlInstance
     */
    public void enqueueTask(StepInstance currentSi, FullEtlInstance fullEtlInstance) {
        log.info("Step: {}", currentSi.getStepInstanceId());
        //TODO are you sure ??
        if (fullEtlInstance == null) {
            try {
                fullEtlInstance = getFullEtlInstance(currentSi.getEtlInstance());
            } catch (Exception e) {
                saveError(currentSi, stepInstanceRepository, e, "cannot calculate logic:");
                return;
            }
        }



        // if ready for step or sensor in Wait
        if (currentSi.getStatus() == null || StepStatus.InWait.name().equals(currentSi.getStatus())) { // not started yet or attempt
            // if all parents Success or Missed or Failed
            String[] parents = currentSi.getParentStepInstanceIds();
            List<StepInstance> parentOkSi = Collections.emptyList();
            if (parents != null) {
                FullEtlInstance finalFullEtlInstance = fullEtlInstance;
                parentOkSi = Arrays.stream(parents)
                        .map(x -> finalFullEtlInstance.getSiBase().get(x))
                        .filter(x -> StepStatus.Success.name().equals(x.getStatus()) ||
                                StepStatus.Missed.name().equals(x.getStatus()) ||
                                StepStatus.Failed.name().equals(x.getStatus()))
                        .toList();
                if (ArrayUtils.isEmpty(parents) || parents.length == parentOkSi.size()) {

                    DataLoading command= currentSi.getStep().getDataLoading();
                    if ( command==null || !currentSi.getActive() || !command.getActive() || command.getId().equals(0) || StringUtils.isEmpty(command.getProps())) {
                        currentSi.addLog("Skip exec because active:" + command.getActive() + " command_Id =" + command.getId() + "commandline:" + command.getProps());
                        currentSi.addLog("no queue task go to step2");
                        stepInstanceRepository.saveAndFlush(currentSi);
                        stepFrom(currentSi);
                    } else {
                        currentSi.addLog("Go to enqueue. command active:" + command.getActive() + "si.active" + currentSi.getActive() +
                                " command_Id =" + command.getId() + "commandline:" + command.getProps());
                        prepareToQueue(currentSi);
                    }
                }

            }
        }
    }


    /**
     * Enqueue step
     *
     * @param si
     * @return
     */
    private boolean prepareToQueue(StepInstance si) {
        log.info("-------!!!!!!startStep: {}", si);
            try {
                si.setStatus(StepStatus.InProcess.name());
                stepInstanceRepository.saveAndFlush(si);
                this.externalService.sendToQueue(si);
                log.info("sent to queue:{}", si.getStepInstanceId());
                return true;
            } catch (Throwable e) {
                saveError(si, stepInstanceRepository, e, "cannot send to queue");
                return true;
            }
    }

}
