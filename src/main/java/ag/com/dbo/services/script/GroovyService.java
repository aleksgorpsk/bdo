package ag.com.dbo.services.script;

import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static ag.com.dbo.services.queue.utils.VarSupport.stringToJsonVar;
import static ag.com.dbo.utils.Utils.objectToString;

@Slf4j
@Service
public class GroovyService {


    public ScriptResponse execGroovyScript(Script script , String vars, String etlResults, String localResults,
                                           String stepName)  {
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
            response.setResponse(objectToString(oResult));
            response.setStatus("OK");
            return response;
        } catch (Exception e) {
            response.setResponse(e.getMessage());
            response.setStatus("ERR");
        }
        return  response;

    }
}
