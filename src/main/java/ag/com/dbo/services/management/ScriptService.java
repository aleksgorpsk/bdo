package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.model.ScriptTest;
import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.*;
import ag.com.dbo.repositories.management.ScriptRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;

import ag.com.dbo.services.queue.model.PropData;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static ag.com.dbo.utils.Utils.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final RestClient scriptRestClient;
    private final StepInstanceRepository stepInstanceRepository;
    private final ObjectMapper objectMapper;
    private final ExternalService externalService;
    private final ModelMapper modelMapper;

    @Value("${script.process.path}")
    private String scriptRunPath;

    public Optional<Script> retrieveById(BigInteger id) {
        return scriptRepository.findById(id);
    }

    public Optional<Script> retrieveByScriptDefinition(ScriptDefinition definition) {
        return scriptRepository.findByScriptDefinition(
                definition.getName(),
                definition.getLanguage(),
                definition.getVersion()
                );

    }

    public Optional<Script> getLastScript(String language, String name) {
        List<Script> l = scriptRepository.getLastScript(language, name);
        if (CollectionUtils.isEmpty(l)) {
            return Optional.empty();
        } else {
            return Optional.of(l.get(0));
        }

    }


// TODO depricated
    /*
    public ScriptDefinition[] getAppropriateScript(StepInstance si, ScriptType type) {
        return Arrays.stream(si.getScript().split(","))
                .filter(StringUtils::isNotEmpty)
                .map(Utils::getScriptDefinitionFromFullName)
                .filter(y -> y.getName() != null)
                .filter(z -> type.name().equals(z.getType()))
                .toArray(ScriptDefinition[]::new);
    }
     */


    /**
     *
     * @param si
     * @return
     */
    public ScriptResponse runScript(StepInstance si, ScriptDefinition scriptDefinition) {//throws JsonProcessingException {
        ScriptResponse response = null;


        si.addLog("RunScript step:" + si.getName() + " script: " + scriptDefinition + " var:" + si.getVars());
        if (si.isShellCommand( scriptDefinition)) {
            CommonModel  shellCommandModel =  si.getScriptModel(ModelName.shellCommandScript);
            boolean resp = this.externalService.prepareToQueue(si, scriptDefinition, shellCommandModel);
            if (resp) {
                return new ScriptResponse(scriptDefinition, Constants.OK, "");
            } else{
                return new ScriptResponse(scriptDefinition, Constants.ERROR, "Cannot send to Queue");
            }
        }
        try {
            response = sendScript(si, scriptDefinition);
            if (!"OK".equals(response.getStatus())) {
                si.addLog("Error in " + scriptDefinition + " " + response);
                si.addLog("RunScript step:" + si.getName() + " script: " + scriptDefinition + " var:" + si.getVars() + " response:" + response);
            }
            return response;
        } catch (JsonProcessingException e) {
            response = new ScriptResponse();
            response.setStatus("ERROR");
            response.setResponse(e.getMessage());
            si.addLog("Error in " + scriptDefinition + " " + e.getMessage());
            return response;
        } finally {
            stepInstanceRepository.saveAndFlush(si);
        }
    }

    /**
     * * script Ok/Not
     *
     * @param si
     * @return
     * @throws JsonProcessingException
     */
    public ScriptResponse sendScript(StepInstance si, ScriptDefinition scriptId) throws JsonProcessingException {
        ScriptRequest scriptRequest = new ScriptRequest();

        scriptRequest.setCommonModel(si.getModelByScriptDefinition(scriptId));
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

    public ScriptResponse sendTestScript(String name , String vars, String localResult, String etlVars, ScriptDefinition scriptDef) throws JsonProcessingException {
        ScriptRequest scriptRequest = new ScriptRequest();

//        scriptRequest.setScriptDefinition(scriptDef);
        scriptRequest.setStepName("test");
        scriptRequest.setVars(vars);
        scriptRequest.setLocalResults(localResult);
        scriptRequest.setEtlResults(etlVars);
//        scriptRequest.setMaxAttempts(1);
        if(ScriptLanguage.SHELL_COMMAND.name().equals(scriptDef.getLanguage())){
           PropData res= externalService.sendTestReqst(scriptRequest);
            return new ScriptResponse(scriptDef,""+ res.getResultStatus() ,res.getMessage());

        }else {
            try {
                return this.scriptRestClient.put().uri(scriptRunPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(scriptRequest)
                        .retrieve()
                        .body(ScriptResponse.class);
            } catch (Throwable e) {
                return new ScriptResponse(scriptDef, "Error", e.getMessage());
            }
        }
    }

    //-- UI -----
    public Page<@NonNull ScriptDto> retrievePage(PageRequest pageable){
        Page<Script> scripts = scriptRepository.findAll(pageable);
        return scripts.map(this::getScriptDto);
    }

    public Page<@NonNull ScriptDto> retrievePage(Pageable pageable){
        Page<@NonNull Script> scripts = scriptRepository.findAll(pageable);
        return scripts.map(this::getScriptDto);

    }

    public Page<@NonNull ScriptDto> findByScriptContainingIgnoreCase(String keyword, Pageable pageable){
        Page<@NonNull Script> etlPage = scriptRepository.findByNameContainingIgnoreCase( keyword,  pageable);
        return convert(etlPage);
    }


    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    public boolean update(ScriptDto scriptDto) {
        if(scriptDto.getId()==null){
            scriptRepository.saveAndFlush(mapFrom(scriptDto));
            return true;
        }
        if (scriptRepository.existsById(scriptDto.getId())) {
            scriptRepository.save(getScript(scriptDto));
            return true;
        } else {
            return false;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Delete
     * -------------------------------------------------------------------------
     */

    public boolean delete(BigInteger sid ) {
        if (scriptRepository.existsById(sid)) {
            scriptRepository.deleteById(sid);
            return true;
        } else {
            return false;
        }
    }

    public Optional<ScriptDto> findById(BigInteger id){
        Optional<Script> e=scriptRepository.findById(id);
        if (e.isPresent()) {
            return Optional.of(getScriptDto(e.get()));
        }
        return Optional.empty();
    }


    public Page<@NotNull ScriptDto> convert(Page<@NotNull Script> script){
        return script.map(new Function<Script, ScriptDto>() {
            @Override
            public ScriptDto apply(Script entity) {
                return getScriptDto(entity);
            }
        });
    }

    public Script getScript(ScriptDto sdto){
        return mapFrom(sdto);
    }


    public ScriptDto getScriptDto(Script s){
        return mapFrom(s);
    }


    public ScriptDto mapFrom(Script scriptl) {
        return modelMapper.map(scriptl, ScriptDto.class);
    }

    public Script mapFrom(ScriptDto dto) {
        return modelMapper.map(dto, Script.class);
    }

    public ScriptTest mapTestFrom(Script scriptl) {
        return modelMapper.map(scriptl, ScriptTest.class);
    }

    public Script mapTestFrom(ScriptTest test) {
        return modelMapper.map(test, Script.class);
    }
}
