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


    public ScriptResponse execGroovyScript(Script script , String vars, String etlResults, String localResults, String stepName ) throws JsonProcessingException {
        ScriptResponse response = new ScriptResponse();
        try {
            Map<String, Object> mapVars = stringToJsonVar(vars);
            Map<String, Object> mapEtlResults = stringToJsonVar(etlResults);
            Map<String, Object> maplocalResults = stringToJsonVar(localResults);
            Binding binding = new Binding();
            binding.setVariable("stepName", stepName);
            binding.setVariable("vars", mapVars);
            binding.setVariable("etlResults", mapEtlResults);
            binding.setVariable("localResults", maplocalResults);
            GroovyShell shell = new GroovyShell(binding);
            Object oResult = shell.evaluate(script.getScript());
            response.setResponse(oResult.toString());
            response.setStatus("OK");
            return response;
        } catch (Exception e) {
            response.setResponse(e.getMessage());
            response.setStatus("ERR");
        }
        return  response;

    }
}
