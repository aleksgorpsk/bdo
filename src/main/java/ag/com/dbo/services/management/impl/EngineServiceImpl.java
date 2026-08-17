package ag.com.dbo.services.management.impl;

import ag.com.dbo.controllers.FullEtlInstance;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.management.*;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;

import ag.com.dbo.services.management.Starter;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ag.com.dbo.services.Utils.*;
import static ag.com.dbo.services.queue.utils.VarSupport.*;
import static ag.com.dbo.utils.Utils.getObjectMapper;
import static ag.com.dbo.utils.Utils.saveError;


@Slf4j
@RequiredArgsConstructor
@Service
public class EngineServiceImpl implements Starter {
    private final EtlInstanceRepository etlInstanceRepository;
    private final StepRepository stepRepository;
    private final StepInstanceRepository stepInstanceRepository;
    private final ScriptServiceImpl scriptService;

    @Value("${dbo.fail.timeout}")
    private Long failTimeoutDefault;

    @Value("${dbo.attempt.timeout}")
    private Long attemptTimeoutDefault;


    public String addDate(EtlInstance ei, boolean scheduling) {
        Map<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("startEtl", OffsetDateTime.now().toString());
        jsonObject.put("scheduling", scheduling);
        jsonObject.put("name", ei.getName());
        jsonObject.put("cronScheduler", ei.getEtl().getCronScheduling());
        try {
            String varStr = getObjectMapper().writeValueAsString(jsonObject);
            return merge(ei.getEtlVars(), varStr, "etl");
        } catch (JsonProcessingException e) {
            ei.addLog("Error: " + e.getMessage());
        }
        return null;
    }

    //Start !!!
    public void startEtl(Etl etl, boolean schedule) {
        if (!etl.getActive()) {
            log.info("etl {} inactive", etl.getId());
            return;
        }
        log.info("Start  etl:{}", etl.getId());
        EtlInstance ei = new EtlInstance();

        ei.setEtl(etl);
        ei.setActive(etl.getActive());
        ei.setName(etl.getName());
        ei.setStart(OffsetDateTime.now());
        ei.setStatus(EtlStatus.InProgress.name());
        ei.setComment(etl.getComment());
        ei.setEtlVars(addDate(ei, schedule));
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
                si.setVars(merge(step.getVars(), etl.getEtlVars()));
                si.setActive(step.getStepActive());
                si.setMaxAttempts(step.getMaxAttempts());
                si.setName(step.getName());
                si.setStatus(StepStatus.NotStartedYet.name());
                si.setTags(step.getTags());
                si.setIsSensor(getStepSensorFlag(si.getVars()));
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
        }
    }

    /**
     * step finished and need new step
     *
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
        // TODO   run script
        stepInstanceRepository.saveAndFlush(si);
        ScriptResponse response = runScripts(si, ModelName.postProcessScript);
        if (Constants.OK.equals(response.getStatus()) || Constants.NO_SCRIPT.equals(response.getStatus())) {
            si.addLog("script postProcessScript passed " + response.getStatus());
        } else {
            saveError(si, stepInstanceRepository, null, "Error in script(s):" + response.getStatus() + ":" + response.getResponse());
            return;
        }


        //TODO  check sensor

        if (StepStatus.InWait.name().equals(si.getStatus()) && si.isSensor()) {
            try {
                ScriptResponse sResp = runScripts(si, ModelName.sensorScript);
                if (!Boolean.parseBoolean(sResp.getResponse())) {
                    si.addLog("Check attempt false " + si.getName() + " : " + sResp);
                    si.setStatus(StepStatus.InWait.name());
                    stepInstanceRepository.saveAndFlush(si);
                    return;
                }
            } catch (Exception e) {
                saveError(si, stepInstanceRepository, e, " Sensor Error");
                return;
            }
        }

        // TODO BRANCH

        Set<String> children = fullEtlInstance.getParentToChildrenStep().get(si.getStepInstanceId());
        List<String> incorrectWayId = null;

        if (si.isBranch()) {
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


        if (CollectionUtils.isEmpty(children)) {
            // check finish
            EtlInstance ei = si.getEtlInstance();
            if (StepStatus.Success.name().equals(si.getStatus())) {
//                all steps success, Failed or missed
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
                } else {
                    if (getEtlInstanceErrorExists(si, fullEtlInstance)) {
                        ei.setStatus(EtlStatus.Fail.name());
                        return;
                    }

                }
            } else if (StepStatus.Failed.name().equals(si.getStatus())) {
                si.setStatus(StepStatus.Failed.name());
                ei.setStatus(EtlStatus.Fail.name());
                stepInstanceRepository.saveAndFlush(si);
                etlInstanceRepository.saveAndFlush(ei);
            }
        }

        if (!CollectionUtils.isEmpty(children)) { // if  children not empty
            FullEtlInstance finalFullEtlInstance = fullEtlInstance;
            List<StepInstance> siList = children.stream()
                    .map(x -> finalFullEtlInstance.getSiBase().get(x))
                    .filter(StepInstance::getActive)
                    .toList();
            si.addLog("Run to queue :" + String.join(",", siList.stream().map(StepInstance::getName).toList()));
            for (StepInstance siCh : siList) {
                enqueueTask(siCh, finalFullEtlInstance);
            }
        } else {
            log.info("last step!:{}", si.getStepInstanceId());
        }
        // Skip alone steps  in incorrect way
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

    public ScriptResponse runOneScript(StepInstance si, ScriptDefinition script) {
        try {
            ScriptResponse scriptResponse = scriptService.runScript(si, script);
            si.addLog("Process script:  " + script + " response: " + scriptResponse);
            return scriptResponse;
        } catch (Exception x) {
            saveError(si, null, x, "Error in :" + script);
            return null;
        }

    }

    //    private List<ScriptResponse> runScripts(StepInstance si, ScriptType scriptType) {
    public ScriptResponse runScripts(StepInstance si, ModelName scriptType) {
        CommonModel scriptModel = si.getScriptModel(scriptType);
        if (scriptModel != null) {
            ScriptDefinition scriptDefinition = getScriptDefinitionByName(scriptModel.getScriptName());
            ScriptResponse response = null;
            try {
                try {
                    response = runOneScript(si, scriptDefinition);
                    if (!Constants.OK.equals(response.getStatus())) {
                        si.addLog("Error send Script:" + response.getResponse());
                        si.setStatus(StepStatus.Failed.name());
                        return response;
                    }
                    try {
                        saveResult(si, response, scriptModel);
                    } catch (JsonProcessingException e) {
                        si.addLog("Cannot parse response: " + " response " + " Error:" + e.getMessage());
                    }
                    si.addLog("Process script : " + scriptDefinition.getFullName());
                    return response;
                } catch (Exception x) {
                    saveError(si, null, x, "Error in :" + scriptDefinition);
                    return response;
                }
            } finally {
                stepInstanceRepository.saveAndFlush(si);
            }
        }
        return new ScriptResponse(null, Constants.NO_SCRIPT, null);
    }

    private List<String> getBranches(StepInstance si) throws Exception {
        CommonModel branchScript = si.getScriptModel(ModelName.branchScript);
//        ScriptDefinition[] scripts = scriptService.getAppropriateScript(si, ScriptType.Branch);
        if (branchScript != null) {
            ScriptDefinition definition = getScriptDefinitionByName(branchScript.getScriptName());
            try {
                ScriptResponse scriptResponse = scriptService.runScript(si, definition);
                if (!Constants.OK.equals(scriptResponse.getStatus())) {
                    log.error("Error send Script:" + scriptResponse.getResponse());
                    si.setStatus(StepStatus.Failed.name());
                    stepInstanceRepository.saveAndFlush(si);
                    return Collections.EMPTY_LIST;
                }
                try {
                    saveResult(si, scriptResponse, branchScript);

                } catch (JsonProcessingException e) {
                    si.addLog("Cannot parse response: " + e.getMessage());
                }
                si.addLog("Process script(s) branch : " + definition);
                stepInstanceRepository.saveAndFlush(si);
                return stringToObject(scriptResponse.getResponse(), List.class);
            } catch (Exception x) {
                saveError(si, stepInstanceRepository, x, "Error in :" + definition);
                return Collections.EMPTY_LIST;
            }
        } else {
            si.addLog("No script for step: " + si.getName());
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


    public List<StepInstance> recursiveSetMissed(FullEtlInstance fullEtlInstance, String stepInstanceId, boolean recursive) {
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

    /**
     * test that all steps Success Missed or Failed and
     *
     * @param si
     * @param fullEtlInstance
     * @return
     */
    private boolean checkAllStepInstances(StepInstance si, FullEtlInstance fullEtlInstance) {
        List<StepInstance> steps = fullEtlInstance.getSteps();

        long finishedCount = steps.stream().filter(x ->
                (!StepStatus.Success.name().equals(x.getStatus()) &&
                        !StepStatus.Missed.name().equals(x.getStatus()) &&
                        !StepStatus.Failed.name().equals(x.getStatus()))
        ).count();

        return (finishedCount == 0);

    }

    /**
     * get count of error steps
     *
     * @param si
     * @param fullEtlInstance
     * @return
     */
    private boolean getEtlInstanceErrorExists(StepInstance si, FullEtlInstance fullEtlInstance) {
        List<StepInstance> steps = fullEtlInstance.getSteps();
        long errorCount = steps.stream()
                .filter(x -> StepStatus.Failed.name().equals(x.getStatus()))
                .count();
        return (errorCount > 0);

    }


    private void makeStep(EtlInstance etlInstance) throws Exception {

        log.info("make first step etlInstance id:{}", etlInstance.getEtlInstanceId());

        FullEtlInstance fullEtlInstance = getFullEtlInstance(etlInstance);
        // root steps can be with condition ? I suppose not.
        List<StepInstance> startSteps = fullEtlInstance.getSteps().stream().filter(x -> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
        startSteps.forEach(x -> enqueueTask(x, fullEtlInstance));
    }


    private FullEtlInstance getFullEtlInstance(EtlInstance etlInstance) {
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
     * need full check because maybe not all parents is ready no fullEtlInstance in sensor scheduling
     *
     * @param currentSi
     * @param fullEtlInstance
     */
    public void enqueueTask(StepInstance currentSi, FullEtlInstance fullEtlInstance) {
        log.info("Step: {}", currentSi.getStepInstanceId());

        if (!StepStatus.NotStartedYet.name().equals(currentSi.getStatus()) && !StepStatus.InWait.name().equals(currentSi.getStatus())) {
            return;
        }
        if (fullEtlInstance == null) {
            try {
                fullEtlInstance = getFullEtlInstance(currentSi.getEtlInstance());
            } catch (Exception e) {
                saveError(currentSi, stepInstanceRepository, e, "cannot calculate logic:");
                return;
            }
        }
        // check if parent is ok
        FullEtlInstance finalFullEtlInstance = fullEtlInstance;
        String[] parents = currentSi.getParentStepInstanceIds();
        List<StepInstance> parentNotProcessed = Collections.EMPTY_LIST;
        if (ArrayUtils.isNotEmpty(parents)) {
            parentNotProcessed = Arrays.stream(parents)
                    .map(x -> finalFullEtlInstance.getSiBase().get(x))
                    .filter(x -> !StepStatus.Success.name().equals(x.getStatus())) // add skipped and failed
                    .toList();
        }
        // if all parent OK
        if (!CollectionUtils.isEmpty(parentNotProcessed)) {
            return;
        }
        if (currentSi.getActive()) {

            if (currentSi.isSensor()) {
                currentSi.setStatus(StepStatus.InWait.name());
                ///  set timing for scheduling
                CommonModel sm = currentSi.getScriptModel(ModelName.sensorScript);
                currentSi.setNextTest(Instant.now().getEpochSecond() + sm.getAttemptTimeOut());
            } else {
                currentSi.setStatus(StepStatus.InProcess.name());
            }
            if (currentSi.getStart() == null) {
                currentSi.setStart(OffsetDateTime.now());
            }
        } else {
            currentSi.addLog("Skip exec because inactive:" + currentSi.getActive());
            currentSi.setStatus(StepStatus.Missed.name());
        }
        stepInstanceRepository.saveAndFlush(currentSi);

        if (CollectionUtils.isEmpty(parentNotProcessed)) {
            currentSi.addLog("Go to enqueue. si.active" + currentSi.getActive());
            runScripts(currentSi, ModelName.prepareScript);
            ScriptResponse sr2 = runScripts(currentSi, ModelName.shellCommandScript);
            if (Constants.NO_SCRIPT.equals(sr2.getStatus())) { // in case no big script
                stepFrom(currentSi);
            }
        }
    }


}
