package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.models.checker.script.ScriptModel;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.models.script.ScriptId;
import ag.com.dbo.models.script.ScriptType;
import ag.com.dbo.repositories.management.ScriptRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.services.Utils;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ag.com.dbo.services.Utils.getScriptDefinitionFromFullName;
import static ag.com.dbo.services.queue.utils.VarSupport.merge;
import static ag.com.dbo.utils.Utils.*;


@Slf4j
@Service
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final RestClient scriptRestClient;
    private final StepInstanceRepository stepInstanceRepository;
    private final ObjectMapper objectMapper;
    private final ExternalService externalService;

    @Value("${script.process.path}")
    private String scriptRunPath;

    public ScriptService(ScriptRepository scriptRepository, RestClient scriptRestClient, StepInstanceRepository stepInstanceRepository, ObjectMapper objectMapper, ExternalService externalService) {
        this.scriptRepository = scriptRepository;
        this.scriptRestClient = scriptRestClient;
        this.stepInstanceRepository = stepInstanceRepository;
        this.objectMapper = objectMapper;
        this.externalService = externalService;
    }

    public Optional<Script> retrieveById(ScriptId id) {
        return scriptRepository.findById(id);

    }

    public Optional<Script> getLastScript(String language, String name) {
        List<Script> l = scriptRepository.getLastScript(language, name);
        if (CollectionUtils.isEmpty(l)) {
            return Optional.empty();
        } else {
            return Optional.of(l.get(0));
        }

    }

    private ScriptModel getScriptModel(StepInstance si) throws JsonProcessingException {
        return getExtendedObjectMapper().readValue(si.getVars(), new TypeReference<>() {
        });
    }


    public ScriptDefinition[] getAppropriateScript(StepInstance si, ScriptType type) {
        return Arrays.stream(si.getScript().split(","))
                .filter(StringUtils::isNotEmpty)
                .map(Utils::getScriptDefinitionFromFullName)
                .filter(y -> y.getName() != null)
                .filter(z -> type.name().equals(z.getType()))
                .toArray(ScriptDefinition[]::new);
    }


    /**
     *
     * @param si
     * @return
     */
    public ScriptResponse runScript(StepInstance si, ScriptDefinition[] scripts) {//throws JsonProcessingException {
        ScriptResponse response = null;
        try {
            si.addLog("RunScript step:" + si.getName() + " script: " + si.getScript() + " var:" + si.getVars());
            if (ArrayUtils.isNotEmpty(scripts)) {

                for (ScriptDefinition scriptId : scripts) {
                    si.addLog("run Script: " + scriptId);
                    try {
                        ScriptModel scriptModel = getScriptModel(si);
                        response = sendScript(si, si.getVars(), si.getLocalResults(), scriptId);
                        if ("OK".equals(response.getStatus())) {
                            if (StringUtils.isNotEmpty(scriptModel.getResultName())) {
                                si.setLocalResults(merge(si.getLocalResults(), response.getResponse(), scriptModel.getResultName()));
                            } else {
                                si.setLocalResults(merge(si.getLocalResults(), response.getResponse()));
                            }
                        } else {
                            si.addLog("Error in " + si.getScript() + " " + response);
                            si.addLog("RunScript step:" + si.getName() + " script: " + si.getScript() + " var:" + si.getVars() + " response:" + response);
                            return response;
                        }
                        return response;
                    } catch (JsonProcessingException e) {
                        response = new ScriptResponse();
                        response.setStatus("ERROR");
                        response.setResponse(e.getMessage());
                        si.addLog("Error in " + si.getScript() + " " + e.getMessage());
                        return response;
                    }
                }
            }
        } finally {
            stepInstanceRepository.saveAndFlush(si);
        }
        return response;
    }

    /**
     * * script Ok/Not
     *
     * @param si
     * @return
     * @throws JsonProcessingException
     */
    public ScriptResponse sendScript(StepInstance si, String vars, String localResults, ScriptDefinition scriptId) throws JsonProcessingException {
        ScriptRequest scriptRequest = new ScriptRequest();

        scriptRequest.setScriptDefinition(scriptId);
        scriptRequest.setStepName(si.getName());
        scriptRequest.setVars(si.getVars());
        scriptRequest.setLocalResults(si.getLocalResults());
        scriptRequest.setEtlResults(si.getEtlInstance().getEtlVars());
        try {
            return this.scriptRestClient.put().uri(scriptRunPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scriptRequest)
                    .retrieve()
                    .body(ScriptResponse.class);
        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to Script executor");
            return new ScriptResponse(scriptId, "Error", e.getMessage());
        }
    }

    public void sendToQueue(StepInstance si) throws JsonProcessingException {
        log.info("sendToQueue:{}", si);
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(si.getStepInstanceId());
        taskRequest.setName(si.getName());
        taskRequest.setCommandProfile(si.getStep().getDataLoading().getProps());
        taskRequest.setCalculateType(si.getStep().getDataLoading().getName());
        taskRequest.setMaxAttempts(si.getStep().getMaxAttempts());
        taskRequest.setVars(si.getVars());
        taskRequest.setLocalResult(si.getLocalResults());
        taskRequest.setEtlResult(si.getEtlInstance().getEtlVars());
        taskRequest.setScript(si.getScript());
        taskRequest.setStepType(si.getStepType());
        taskRequest.setResults(si.getEtlInstance().getEtlVars());
        externalService.sendToQueue(taskRequest, si);
    }

}
