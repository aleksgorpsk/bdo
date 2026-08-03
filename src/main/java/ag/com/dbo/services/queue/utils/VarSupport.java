package ag.com.dbo.services.queue.utils;

import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.models.script.ScriptType;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ag.com.dbo.utils.Utils.getObjectMapper;

@Slf4j
public class VarSupport {


    public static  JsonNode stringToVars(String s) throws JsonProcessingException {
        return getObjectMapper().readTree(s);
    }

    public static Object getVar(JsonNode node, String name){
        return node.get(name);
    }

    public static String merge(JsonNode node,String newData){
        return null;
    }

    public static String merge(String existsData, String newData, String stepName) throws JsonProcessingException {
        if(ObjectUtils.isEmpty(existsData)){
            existsData ="{}";
        }
        if (ObjectUtils.isEmpty(newData)){
            return existsData;
        }
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode targetNode = objectMapper.readTree(existsData);
        String wrapper =  "{\""+ stepName+"\":"+newData+"}";
        JsonNode mergedNode = objectMapper.readerForUpdating(targetNode).readTree(wrapper);
        return objectMapper.writeValueAsString(mergedNode);
    }


    public static String merge(String existsData, String newData) throws JsonProcessingException {
        if(ObjectUtils.isEmpty(existsData)){
            existsData ="{}";
        }
        if (ObjectUtils.isEmpty(newData)){
            return existsData;
        }
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode targetNode = objectMapper.readTree(existsData);
        JsonNode mergedNode = objectMapper.readerForUpdating(targetNode).readTree(newData);
        return objectMapper.writeValueAsString(mergedNode);
    }

    public static Map<String,Object> stringToJsonVar(String s) throws JsonProcessingException {
        if (ObjectUtils.isEmpty(s)){
            return null;
        }
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<>() {};
        return getObjectMapper().readValue(s, typeRef);
    }

    public static <T> T stringToObject(String s, Class<T>  objectType) throws JsonProcessingException {
        if (ObjectUtils.isEmpty(s)){
            return null;
        }
        return getObjectMapper().readValue(s, objectType);
    }


    public static void saveResult(StepInstance si, ScriptResponse result) throws JsonProcessingException {
        ScriptDefinition scriptId = result.getScriptDefinition();
        if (ScriptType.Common.name().equals(scriptId.getType())){
            si.setLocalResults( merge(si.getLocalResults(), result.getResponse()));
        }else if (ScriptType.Branch.name().equals(scriptId.getType())){
            si.setLocalResults( merge(si.getLocalResults(),result.getResponse(), Constants.BRANCH_RESULT_NAME));
        }else if (ScriptType.Sensor.name().equals(scriptId.getType())){
            si.setLocalResults( merge(si.getLocalResults(),result.getResponse(), Constants.SENSOR_RESULT_NAME));
        }else if (ScriptType.ShellCommand.name().equals(scriptId.getType())){
            si.setLocalResults( merge(si.getLocalResults(), result.getResponse()));
        }else if (ScriptType.PreExecution.name().equals(scriptId.getType())){
            si.setLocalResults( merge(si.getLocalResults(), result.getResponse()));
        }else {
            String err= "Cannot define result to localVars";
            log.error("{} {} scriptId:",err, scriptId);
            si.addLog(err+ " scriptId: "+scriptId);
        }
    }

}
