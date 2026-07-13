package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.model.ScriptResponses;
import ag.com.dbo.models.checker.script.DirtyScriptModel;
import ag.com.dbo.models.checker.script.ScriptModel;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptId;
import ag.com.dbo.repositories.management.ScriptRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static ag.com.dbo.services.queue.utils.VarSupport.merge;
import static ag.com.dbo.utils.Utils.getExtendedObjectMapper;
import static ag.com.dbo.utils.Utils.saveError;


@Slf4j
@Service
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final RestClient scriptRestClient;
    private final StepInstanceRepository stepInstanceRepository;
    private final ObjectMapper objectMapper;

    @Value("${script.process.path}")
    private String scriptRunPath;

    public ScriptService(ScriptRepository scriptRepository, RestClient scriptRestClient, StepInstanceRepository stepInstanceRepository, ObjectMapper objectMapper) {
        this.scriptRepository = scriptRepository;
        this.scriptRestClient = scriptRestClient;
        this.stepInstanceRepository = stepInstanceRepository;
        this.objectMapper = objectMapper;
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

    private DirtyScriptModel getDirtyScriptModel(StepInstance si) throws JsonProcessingException {
        return  getExtendedObjectMapper().readValue(si.getVars(), new TypeReference<>(){});
    }

    private ScriptId getScriptName(String fullScriptName){
        ScriptId result = new ScriptId();
        String[] ar =fullScriptName.split(":");
        result.setLanguage(ar[0]);
        result.setName(ar[1]);
        if(ar.length>2){
            result.setVersion(ar[2]);
        }
        return  result;
    }
    public StepInstance runScript(StepInstance si) throws JsonProcessingException {

        DirtyScriptModel dirtyScriptModel = getDirtyScriptModel(si);
        if (StringUtils.isNotEmpty(si.getScript())) {

            String[] scripts =si.getScript().split(",");
            si.addLog("run Scripts: " + si.getScript());
            ScriptResponses responses= new ScriptResponses();
            ObjectNode jsonObject = objectMapper.createObjectNode();
            for(String script : scripts) {

                ScriptId  scriptName = getScriptName(script);
                si.addLog("run Script: " +script);
                ScriptModel  scriptModel = dirtyScriptModel.getScriptModel(scriptName.getName());
                ScriptResponse response = sendScript(si, scriptModel, si.getVars(), si.getLocalResults(), scriptName.getName());
                responses.addResponse(response);
                String jsonResponse = objectMapper.writeValueAsString(response);
                jsonObject.put(scriptModel.getResultName(),jsonResponse);

            }
            String newString = objectMapper.writeValueAsString(jsonObject);
            si.setLocalResults(merge(si.getLocalResults(), newString));
            return stepInstanceRepository.saveAndFlush(si);
        }
        return si;
    }

     /**
      *  * script Ok/Not
     *
     * @param si
     * @param sModel
     * @return
             * @throws JsonProcessingException
     */
    public ScriptResponse sendScript(StepInstance si, ScriptModel sModel, String vars, String localResults, String name) throws JsonProcessingException {
        ScriptRequest scriptRequest = sModel.scriptRequest(si.getScript());

        scriptRequest.setVars(vars);
        scriptRequest.setResultName(sModel.getResultName());
        scriptRequest.setScriptName(name);
        scriptRequest.setStepName(si.getName());
        scriptRequest.setLocalResults(localResults);
        scriptRequest.setEtlResults(si.getEtlInstance().getEtlVars());

        try {
            return this.scriptRestClient.put().uri(scriptRunPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scriptRequest)
                    .retrieve()
                    .body(ScriptResponse.class);
        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to Script executor");
            return new ScriptResponse(name,"Error", e.getMessage());
        }
    }
}
