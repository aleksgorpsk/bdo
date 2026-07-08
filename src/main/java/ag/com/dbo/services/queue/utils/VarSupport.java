package ag.com.dbo.services.queue.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ObjectUtils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ag.com.dbo.utils.Utils.getObjectMapper;

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
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
        return getObjectMapper().readValue(s, typeRef);
    }

    public static List<String> stringBranchVars(Object o) throws JsonProcessingException {
        if (o==null){
            return null;
        }
        String s = o.toString();
        if (s.isEmpty()){
            return null;
        }
        TypeReference<List<String>> typeRef = new TypeReference<>() { };
        return getObjectMapper().readValue(s, typeRef);
    }
}
