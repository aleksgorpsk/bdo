package ag.com.dbo.services.script;

import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.script.Script;
import com.fasterxml.jackson.core.JsonProcessingException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static ag.com.dbo.services.queue.utils.VarSupport.stringToJsonVar;

@Slf4j
@Service
public class GroovyService {


    public ScriptResponse execSensorBranchGroovyScript(Script script , String inVars, String inResults, String StepName ) throws JsonProcessingException {
        ScriptResponse response = new ScriptResponse();
        try {
            Map<String, Object> vars = stringToJsonVar(inVars);
            Map<String, Object> results = stringToJsonVar(inResults);
            Binding binding = new Binding();
            binding.setVariable("stepName", StepName);
            binding.setVariable("vars", vars);
            binding.setVariable("results", results);
            GroovyShell shell = new GroovyShell(binding);
            Object oResult = shell.evaluate(script.getScript());
            String res= "{\"result\":"+oResult.toString()+"}";
            response.setResponse(res);
            response.setStatus("OK");
            return response;
        } catch (Exception e) {
            response.setResponse(e.getMessage());
            response.setStatus("ERR");
        }
        return  response;

    }
}
