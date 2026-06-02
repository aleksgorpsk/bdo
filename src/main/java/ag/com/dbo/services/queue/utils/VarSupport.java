package ag.com.dbo.services.queue.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.thymeleaf.util.StringUtils;

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

    public static String merge(String existsData,String newData) throws JsonProcessingException {
        if(StringUtils.isEmpty(existsData)){
            existsData ="{}";
        }
        if (StringUtils.isEmpty(newData)){
            return existsData;
        }
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode targetNode = objectMapper.readTree(existsData);
//        JsonNode sourceNode = objectMapper.readTree(newData);
        JsonNode mergedNode = objectMapper.readerForUpdating(targetNode).readTree(newData);
        return objectMapper.writeValueAsString(mergedNode);
    }

}
