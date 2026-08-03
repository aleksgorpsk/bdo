package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.script.*;
import ag.com.dbo.services.management.ScriptService;
import ag.com.dbo.services.script.GroovyService;
import ag.com.dbo.services.script.PythonService;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static ag.com.dbo.utils.Utils.getScriptIdString;

@RestController
//@RequestMapping("/queue")
@Slf4j
public class ScriptController {

    private final ScriptService scriptService;
    private final GroovyService groovyService;
    private final PythonService pythonService;


    public ScriptController(ScriptService scriptService, GroovyService groovyService, PythonService pythonService) {
        this.scriptService = scriptService;
        this.groovyService = groovyService;
        this.pythonService = pythonService;
    }

    @PutMapping("/script/run")
    public ResponseEntity<@NotNull ScriptResponse> runScript(
            @RequestBody ScriptRequest request) throws JsonProcessingException {
        log.info("ScriptRequest :{}", request);


        Script script = null;
        if (request.getScriptDefinition().getVersion() == null) {
            Optional<Script> sc = scriptService.getLastScript(request.getScriptDefinition().getLanguage(), request.getScriptDefinition().getName());
            if (sc.isPresent()) {
                script = sc.get();
            }
        } else {
            ScriptDefinition definition = request.getScriptDefinition();

            Optional<Script> sc = scriptService.retrieveByScriptDefinition(definition);
            if (sc.isPresent()) {
                script = sc.get();
            }

        }
        if (script != null) {
            if (ScriptLanguage.GROOVY.name().equals(script.getLanguage())) {
                ScriptResponse response = groovyService.execGroovyScript(script, request.getVars(), request.getEtlResults(), request.getLocalResults(), request.getStepName());
                response.setScriptDefinition(request.getScriptDefinition());
                return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
            } else if (ScriptLanguage.PYTHON.name().equals(script.getLanguage())) {
                ScriptResponse response = pythonService.execPythonScript(script, request.getVars(), request.getEtlResults(), request.getLocalResults(), request.getStepName());
                response.setScriptDefinition(request.getScriptDefinition());
                return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
            }else if (ScriptLanguage.SHELL_COMMAND.name().equals(script.getLanguage())) {
                    ScriptResponse  response = pythonService.execPythonScript(script, request.getVars(), request.getEtlResults(), request.getLocalResults(), request.getStepName());
                    response.setScriptDefinition(request.getScriptDefinition());
                    return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
            } else {
                ScriptResponse response = new ScriptResponse();
                response.setStatus(getScriptIdString(script) + " Not Implemented yet.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(response);
            }
        } else {
            ScriptResponse response = new ScriptResponse();
            response.setScriptDefinition(request.getScriptDefinition());
            response.setStatus(Constants.ERROR);
            response.setResponse(request.getScriptDefinition()+" not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type", "application/json").body(response);
        }
    }
}
