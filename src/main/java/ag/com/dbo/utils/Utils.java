package ag.com.dbo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Map;

public class Utils {

    public static ObjectMapper getObjectMapper(){
        return JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }

    public static Map<String, Object> getMap(String jsonString) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonString, new TypeReference<>() {});
    }

    public static String objectToString( Object input) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(input);
    }

}
