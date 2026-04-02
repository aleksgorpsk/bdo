package ag.com.dbo.services.loadingService.model;

import ag.com.dbo.models.Step;
import ag.com.dbo.models.StepInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
@Data

public class HiveTask<String> implements Callable<String> {
    private StepInstance inn;
    private String out;
    public HiveTask(StepInstance data){
        this.inn = data;
    }


    private boolean readStr(BufferedReader reader, java.lang.String expected) throws IOException {
        String fullLine= (String) "";
        java.lang.String line;
        while ((line = reader.readLine()) != null) {
            log.info("line:{}", line);
            fullLine = (String) (fullLine + line+System.lineSeparator());
            if (line.contains(expected)) {
                return true;
            }
        }
        log.info("full line:{}", line);

      return false;
    }

    private Map<String,Object> getMap(java.lang.String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
    }

    private Integer getInt(Map<String,Object> map, java.lang.String varName){
        Object value=map.get(varName);
        if (value == null){
            log.error("cannot find {}", varName);
            return null;
        }
        return Integer.parseInt(value.toString());
    }
    private String getString(Map<String,Object> map, java.lang.String varName){
        Object value=map.get(varName);
        if (value == null){
            log.error("cannot find {}", varName);
            return null;
        }
        return (String) value.toString();
    }
    private java.lang.String applyVars(java.lang.String property, Map<java.lang.String,java.lang.String> vars){
        if (property==null){
            return null;
        }
        java.lang.String result = property;
        for (java.lang.String var:vars.keySet()){
            java.lang.String template = "\\$\\{"+var+"\\}";
            result=result.replaceAll(template,vars.get(var) );
        }
        return result;
    }
    @Override
    public String call() throws Exception {

        try {
            // Define the complex bash command to run
            java.lang.String varJson = inn.getStep().getVars();
            Step step = inn.getStep();
            log.info("Start hiveserver2");
            Map vars = getMap(step.getVars());
            Map logic = getMap(step.getDataLoading().getProps());
            Map<String,String> var= (Map<String, String>) vars.get("props");
            log.info("vars:{}", vars);
            log.info("info:{}", logic);

            if (logic==null){
                log.error("No logic json!");
                return (String) "No logic json!";
            }
            int exitCode=-100;
            Map<String,Map<String,String>> props= (Map<String, Map<String, String>>) logic.get("props");
            int stepcount= getInt(logic,"stepCount").intValue();
            java.lang.String command = (java.lang.String) getString(logic,"command");
            if (stepcount>0) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("bash", "-c", (java.lang.String) command);
                processBuilder.redirectErrorStream(true); // Combine stdout and stderr
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                OutputStream output = process.getOutputStream();

                for (int i = 1; i <= stepcount; i++) {
                    Map<String,String> property = props.get(""+i);
                    if (property==null){
                        log.error("incorrect property {} in{}" ,i, property );
                        return (String) "incorrect property";
                    }
                    java.lang.String in = (java.lang.String) property.get("in");
                    java.lang.String out = (java.lang.String) property.get("out");
                    out=applyVars(out, (Map<java.lang.String, java.lang.String>) vars.get("props"));

                    if (in != null){
                        log.info("in:{}{}",i, in);
                        readStr(reader, in);
                    } else if (out!=null) {
                        log.info("out:{}{}",i, out);
                        output.write((out+System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
                        output.flush();
                    }else{
                        log.error("out:{}{}",i , property);
                    }
                }
                exitCode = process.waitFor();
            }
            System.out.println("\nExited with code : " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        out = (String) (inn + " Result");
        return out;
    }
}
