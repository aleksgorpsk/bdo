package ag.com.dbo.services.queue.model;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.TaskProperties;
import ag.com.dbo.utils.Utils;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.env.Environment;

@Slf4j
public class SimpleBashTask extends TaskProperties implements Callable<PropData> {
    /**
     *  errors:
     *  -105 : too many attempts
     *  -106 : process error
     */
    private final QueueStorage task;
    private String out;
    private final  Environment env;
    private final QueueStorageRepository queueStorageRepository;

    public SimpleBashTask(QueueStorage data, Environment env, QueueStorageRepository queueStorageRepository){
        super(env);
        this.queueStorageRepository = queueStorageRepository;
        task = data;
        this.env = env;
    }

    @Override
    public PropData call() throws Exception {

        try {
            task.setStatus(QueueStatus.IN_PROGRES.name());

            task.setAttempt( (task.getAttempt()==null)?1:task.getAttempt()+1 );
            if (task.getAttempt() > task.getMaxAttempts()){
                String error = "too many attempts:"+task.getAttempt();
                task.addLog(error);
                task.setStatus(QueueStatus.FAIL.name());
                task.setStop(OffsetDateTime.now());
                queueStorageRepository.saveAndFlush(task);
                return new PropData(-105, task, error);
            }
            task.setStart(OffsetDateTime.now());
            queueStorageRepository.saveAndFlush(task);

            String sVars=task.getParameters();
            Map<String, String > vars = new HashMap<>();
            if (sVars!= null){
                vars = Utils.getMap(sVars);
            }

            String logic = task.getCommandProfile();
            log.info("Start hiveServer2 vars:{} {} logic:{}", vars,System.lineSeparator(),logic);

            if (logic == null){
                log.error("No logic !");
                PropData pd = new PropData();
                return new PropData(-106,  task,"Error in command: "+logic);
            }

            if (!Collections.isEmpty(vars)){
                for (Map.Entry<String, String> entry: vars.entrySet()){
                    logic = logic.replace("{"+entry.getKey()+"}",entry.getValue());
                }
            }

            log.info("logic: {}", logic);
            int exitCode=-100;

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", logic);
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            OutputStream output = process.getOutputStream();
            out=applyVars(out,  vars);


            String log = fullReadStr(reader);
            int processCode = process.waitFor();
            log = log+ System.lineSeparator()+ " code result: "+processCode;

            if (processCode == 0){
                //        task.setResultCode();
                task.setStatus(QueueStatus.SUCCESS.name());
            }else{
                task.setStatus(QueueStatus.FAIL.name());
            }

            task.addLog(log);
            task.setStop(OffsetDateTime.now());
            queueStorageRepository.saveAndFlush(task);
            return new PropData(processCode, task, out);


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            task.addLog("Error: " + e.getMessage());
            task.setStop(OffsetDateTime.now());
            queueStorageRepository.saveAndFlush(task);
            return new PropData(-106, task, System.lineSeparator()+ ExceptionUtils.getStackTrace(e) + e.getMessage());
        }
    }
}
