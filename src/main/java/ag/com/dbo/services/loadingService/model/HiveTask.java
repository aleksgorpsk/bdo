package ag.com.dbo.services.loadingService.model;

import ag.com.dbo.models.Step;
import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@Slf4j
@Data

public class HiveTask<Object> implements Callable<Object> {
    private StepInstance inn;
    private String out;

    private final  Environment env;
    private final StepInstanceRepository stepInstanceRepository;

    public HiveTask(StepInstance data, Environment env, StepInstanceRepository stepInstanceRepository){
        inn= data;
        this.env = env;
        this.stepInstanceRepository = stepInstanceRepository;
    }
    @Autowired

    private boolean readStr(BufferedReader reader, String expected) throws IOException {
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
    private void fullReadStr(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            log.info("line:{}", line);
        }
    }

    private Map<String,Object> getMap(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<>() {});
    }

    private Integer getInt(Map<String,Object> map, String varName){
        Object value=map.get(varName);
        if (value == null){
            log.error("cannot find {}", varName);
            return null;
        }
        return Integer.parseInt(value.toString());
    }
    private String getString(Map<String,Object> map, String varName){
        Object value=map.get(varName);
        if (value == null){
            log.error("cannot find {}", varName);
            return null;
        }
        return  value.toString();
    }
    private String applyVars(String property, Map<String,String> vars){
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
    @Override
    public Object call() throws Exception {

        try {
            inn.setStatus(1);
            stepInstanceRepository.saveAndFlush(inn);
            // Define the complex bash command to run
            String varJson = inn.getStep().getVars();
            Step step = inn.getStep();
            log.info("Start hiveserver2");
            Map<String, Object> vars = getMap(step.getVars());
            Map<String, Object> logic = getMap(step.getDataLoading().getProps());
          //  Map<String,String> var= (Map<String, String>) vars.get("props");
            log.info("vars:{}", vars);
            log.info("info:{}", logic);

            if (logic==null){
                log.error("No logic json!");
                return (Object) "No logic json!";
            }
            int exitCode=-100;
            Map<String,Map<String,String>> props= (Map<String, Map<String, String>>) logic.get("props");
            int stepcount= getInt(logic,"stepCount");
            String command =  getString(logic,"command");
            if (vars!= null && command!= null && vars.get("props") != null) {
                command = applyVars(command, (Map<String, String>) vars.get("props"));
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            OutputStream output = process.getOutputStream();

            if (stepcount>0) {
                for (int i = 1; i <= stepcount; i++) {
                    Map<String,String> property = props.get(""+i);
                    if (property==null){
                        log.error("incorrect property {} in{}" ,i, property );
                        return (Object) "incorrect property";
                    }
                    String in =  property.get("in");
                    String out =  property.get("out");
                    out=applyVars(out, (Map<String, String>) vars.get("props"));

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
            }
            fullReadStr(reader);
            exitCode = process.waitFor();
            System.out.println("\nExited with code : " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        out = inn + " Result";
        return (Object) out;
    }
}
