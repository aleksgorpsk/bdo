package ag.com.dbo.services.loadingService.model;

import org.springframework.core.env.Environment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class TaskProperties {

    private final Environment env;

    public TaskProperties(Environment env) {
        this.env = env;
    }

    protected boolean readStr(BufferedReader reader, String expected) throws IOException {
        String fullLine= "";
        String line;
        while ((line = reader.readLine()) != null) {
            log.info("line:{}", line);
            fullLine =  (fullLine + line+System.lineSeparator());
            if (line.contains(expected)) {
                return true;
            }
        }
        log.info("full line:{}", line);

        return false;
    }
    protected void fullReadStr(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            log.info("line:{}", line);
        }
    }

    protected Map<String,Object> getMap(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<>() {});
    }

    protected Integer getInt(Map<String,Object> map, String varName){
        Object value=map.get(varName);
        if (value == null){
            log.error("cannot find {}", varName);
            return null;
        }
        return Integer.parseInt(value.toString());
    }
    protected String getString(Map<String,Object> map, String varName){
        Object value=map.get(varName);
        if (value == null){
            log.error("cannot find {}", varName);
            return null;
        }
        return  value.toString();
    }
    protected String applyVars(String property, Map<String,String> vars){
        if (property==null){
            return null;
        }
        String result = property;
        for (Map.Entry<String, String> entry: vars.entrySet()){
            String template = "\\$\\{" + entry.getKey() + "\\}";

            if(entry.getValue().toUpperCase().startsWith("ENV")){
                String varTemplate =  entry.getValue().replaceAll("ENV.","");
                log.info("varTemplate : {}", varTemplate);
                String varValue= env.getProperty(varTemplate);
                log.info("varValue : {}", varValue);
                if (varValue!=null) {
                    result = result.replaceAll(template, varValue);
                }else{
                    log.warn("varValue : is null");
                }
            }else {
                result = result.replaceAll(template, entry.getValue());
            }
        }
        return result;
    }
}
