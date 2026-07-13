package ag.com.dbo.services.script;

import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.script.Script;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

import static ag.com.dbo.services.queue.utils.VarSupport.stringToJsonVar;
import static ag.com.dbo.utils.Utils.getObjectMapper;

@Slf4j
@Service
public class PythonService {


    public ScriptResponse execPythonScript(Script script , String vars, String etlResults, String localResults, String stepName) throws JsonProcessingException {
        ScriptResponse response = new ScriptResponse();
        try(Context context = Context.newBuilder("python")
                .allowAllAccess(true).build()) {
            ObjectMapper om =getObjectMapper();
            TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};

                context.eval("python", script.getScript());
                Value pythonFn = context.getBindings("python").getMember("parse");

                Map<String, Object> mapVars = stringToJsonVar(vars);
                Map<String, Object> mapEtlResults = stringToJsonVar(etlResults);
                Map<String, Object> maplocalResults = stringToJsonVar(localResults);

                String pyResult = pythonFn.execute( stepName, mapVars, mapVars, mapEtlResults, maplocalResults ).asString();

                response.setStatus("OK");
                response.setResponse(pyResult);
                return  response;

            } catch (Exception e) {
            response.setResponse(e.getMessage());
            response.setStatus("ERR");
        }
        return  response;

    }
}
