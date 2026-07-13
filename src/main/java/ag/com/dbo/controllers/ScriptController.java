package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptId;
import ag.com.dbo.models.script.ScriptType;
import ag.com.dbo.services.management.ScriptService;
import ag.com.dbo.services.script.GroovyService;
import ag.com.dbo.services.script.PythonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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
        if (request.getVersion() == null) {
            Optional<Script> sc = scriptService.getLastScript(request.getLanguage(), request.getScriptName());
            if (sc.isPresent()) {
                script = sc.get();
            }
        } else {
            ScriptId id = new ScriptId(request.getLanguage(), request.getScriptName(), request.getVersion());
            Optional<Script> sc = scriptService.retrieveById(id);
            if (sc.isPresent()) {
                script = sc.get();
            }

        }
        if (script != null) {
            if (ScriptType.GROOVY.name().equals(script.getScriptId().getLanguage())) {
                ScriptResponse response = groovyService.execGroovyScript(script, request.getVars(), request.getEtlResults(), request.getLocalResults(),request.getStepName());
                return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
            }
            if (ScriptType.PYTHON.name().equals(script.getScriptId().getLanguage())) {
                ScriptResponse  response = pythonService.execPythonScript(script, request.getVars(), request.getEtlResults(), request.getLocalResults(), request.getStepName());
                return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
            } else {
                ScriptResponse response = new ScriptResponse();
                response.setStatus(script.getScriptId() + " Not Implemented yet.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(response);
            }
        } else {
            ScriptResponse response = new ScriptResponse();
            response.setStatus(request.getLanguage() + ":" + request.getScriptName() + ":" + request.getVersion() + " Not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type", "application/json").body(response);
        }
    }

}
