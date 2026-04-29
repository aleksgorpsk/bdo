package ag.com.dbo.services.loadingService.model;

import ag.com.dbo.models.Step;
import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.core.env.Environment;

@Slf4j
public class HiveTask extends TaskProperties implements Callable<PropData> {

    private StepInstance inn;
    private String out;
    private final  Environment env;
    private final StepInstanceRepository stepInstanceRepository;

    public HiveTask(StepInstance data, Environment env, StepInstanceRepository stepInstanceRepository){
        super(env);
        inn= data;
        this.env = env;
        this.stepInstanceRepository = stepInstanceRepository;
    }

    @Override
    public PropData call() throws Exception {

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
                PropData pd = new PropData();
                pd.setObj("No logic json!");
                return new PropData();
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

                        PropData pd = new PropData();
                        pd.setObj("incorrect property: "+i+" in "+ property);
                        return pd;
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
            PropData result = new PropData(process.waitFor(),"\nExited with code : " + process.waitFor());
            System.out.println("\nExited with code : " + result.getResultStatus());
            return result;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        out = inn + " Result";
        return new PropData(0, out = inn + " Result");
    }
}
