package ag.com.dbo.utils;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;


import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Slf4j
public class Utils {

    public static ObjectMapper getObjectMapper() {
        return JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }

    public static ObjectMapper getExtendedObjectMapper() {
        return JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public static Map<String, Object> getMap(String jsonString) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonString, new TypeReference<>() {
        });
    }

    public static String objectToString(Object input) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(input);
    }


    public static Map<String, Object> objectToMap(Object o) {
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<>() {};
        return getObjectMapper().convertValue(o, typeRef);
    }
    public static JsonNode objectToNode(Object o) {
        TypeReference<JsonNode> typeRef = new TypeReference<>() {};
        return getObjectMapper().convertValue(o, typeRef);
    }


    public static Map<String,Object> changeFiledName(Map<String,Object> data, String oldName, String newName){
            if (data.containsKey(oldName)) {
                data.put(newName, data.get(oldName));
                data.remove(oldName);
            }
            return data;
    }

    public static void saveError(StepInstance si, StepInstanceRepository stepInstanceRepository, Throwable e, String message) {
        String error = "";
        if (e != null) {
            if (log.isDebugEnabled()) {
                String errorTrace = getStackTrace(e);
                si.addLog(OffsetDateTime.now() + errorTrace);
            }
            error = OffsetDateTime.now() + " - " + message + ": " + e.getMessage();
        } else {
            error = OffsetDateTime.now() + " - " + message;
        }
        si.addLog(error);
        si.setStatus(StepStatus.Failed.name());
        if (stepInstanceRepository!=null) {
            stepInstanceRepository.saveAndFlush(si);
        }

    }

    public static void saveError(QueueStorage queue, QueueStorageRepository queueStorageRepository, Throwable e, String message) {
        String error="";
        if (e!=null) {
            String errorTrace = getStackTrace(e);
            queue.addLog(OffsetDateTime.now() + errorTrace);
            error = OffsetDateTime.now() + ": " + message + ": " + e.getMessage();
        }else{
            error = OffsetDateTime.now() + ": " + message ;
        }
        queue.addLog(error);
        queue.setStatus(QueueStatus.FAIL.name());
        queueStorageRepository.saveAndFlush(queue);
    }

    public static String wrapVars(String wars, String wrapName){
        return "{\""+ wrapName +"\":"+wars+"}";

    }

    public static <T> boolean isArrayNullOrEmpty(T[] theArray) {
        return theArray == null || theArray.length == 0;
    }


    public static String getScriptIdString(Script script) {
        return script.getLanguage() + ":" + script.getName() + "." + script.getType() + ":" + script.getVersion();
    }
}