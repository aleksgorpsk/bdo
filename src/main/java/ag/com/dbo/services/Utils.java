package ag.com.dbo.services;

import ag.com.dbo.models.checker.SensorModel;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepType;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ag.com.dbo.services.queue.utils.VarSupport.*;
import static ag.com.dbo.utils.Utils.getExtendedObjectMapper;
import static ag.com.dbo.utils.Utils.getObjectMapper;

public class Utils {
    public static boolean isContainsStepType(StepInstance si, StepType type){
        if (StringUtils.isEmpty(si.getStepType())){
            return false;
        }
        return si.getStepType().contains(type.name());
    }

    public static List<String> getCorrectBranches(StepInstance si){
        if (StringUtils.isEmpty(si.getVars())){
            return Collections.emptyList();
        }

        ReadContext ctx = JsonPath.parse(si.getVars());
        return ctx.read("$."+si.getName()+".branches.*");
    }


    public static SensorModel getSensorModel(String vars) throws JsonProcessingException {
        return getExtendedObjectMapper().readValue(vars, new TypeReference<>(){});

    }

    public static List<String> getBranches(StepInstance si) throws JsonProcessingException {
       Map<String,Object> result= stringToJsonVar(si.getLocalResults());
       if (result==null){
           return Collections.EMPTY_LIST;
       }
       Object obranch = result.get(Constants.BRANCH_RESULT_NAME);
       if(obranch instanceof List){
           return (List)obranch;
       }else if (obranch instanceof String){ // in case one variant
           return List.of((String)obranch);
       }else {

           si.addLog("No data for "+Constants.BRANCH_RESULT_NAME+ " in localResult: "+si.getLocalResults());
           return null;
       }
    }



    public static ScriptDefinition getScriptDefinition(Script script) {
        ScriptDefinition result = new ScriptDefinition();
        result.setLanguage(script.getLanguage());
        result.setVersion(script.getVersion());
        result.setName(script.getName());
        result.setType(script.getType());
        return result;
    }

    public static ScriptDefinition getScriptDefinitionFromFullName(String fullScriptName) {
        ScriptDefinition result = new ScriptDefinition();
        String[] ar = fullScriptName.split(":");
        result.setLanguage(ar[0]);
        String name= ar[1];
        result.setName(name);
        if(name.contains(".")){
            String[] nameWithExtension = name.split("\\.");
            result.setName(nameWithExtension[0]);
            result.setType(nameWithExtension[1]);
        }else{ //TODO legacy, remove !
            result.setName(name);
        }
        if (ar.length > 2) {
            result.setVersion(ar[2]);
        }
        return result;
    }

    public static void addVarToStep(StepInstance step, String varName, Long value) throws JsonProcessingException {
        ObjectMapper objectMapper = getObjectMapper();
        ObjectNode newVar = objectMapper.createObjectNode();
        newVar.put(varName, value);
        step.setVars(merge(step.getVars(), objectMapper.writeValueAsString(newVar)));
    }


}
