package ag.com.dbo.services.queue;

import org.springframework.core.env.Environment;
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


    protected String fullReadStr(BufferedReader reader) throws IOException {
        StringBuilder fullLog= new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            fullLog.append(System.lineSeparator()).append(line);
        }
        return fullLog.toString();
    }


    protected String applyVars(String property, Map<String,Object> vars){
        if (property==null){
            return null;
        }
        String result = property;
        for (Map.Entry<String, Object> entry: vars.entrySet()){
            String template = "\\$\\{" + entry.getKey() + "\\}";

            if(entry.getValue().toString().toUpperCase().startsWith("ENV")){
                String varTemplate =  entry.getValue().toString().replaceAll("ENV.","");
                log.info("varTemplate : {}", varTemplate);
                String varValue= env.getProperty(varTemplate);
                log.info("varValue : {}", varValue);
                if (varValue!=null) {
                    result = result.replaceAll(template, varValue);
                }else{
                    log.warn("varValue : is null");
                }
            }else {
                result = result.replaceAll(template, entry.getValue().toString());
            }
        }
        return result;
    }
}
