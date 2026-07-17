package ag.com.dbo.services.queue.model;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.models.script.ScriptType;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.ResultTemplate;
import ag.com.dbo.services.queue.TaskProperties;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.Callable;

import static ag.com.dbo.services.queue.utils.VarSupport.*;
import static ag.com.dbo.utils.Utils.saveError;

@Slf4j
public class HiveOperatorTask extends TaskProperties implements Callable<PropData> {
    /**
     *  errors:
     *  -105 : too many attempts
     *  -106 : process error
     */
    protected final QueueStorage task;
    protected String out;
    protected final Environment env;
    protected final QueueStorageRepository queueStorageRepository;


    public HiveOperatorTask(QueueStorage data, Environment env, QueueStorageRepository queueStorageRepository){
        super(env);
        this.queueStorageRepository = queueStorageRepository;
        this.task = data;
        this.env = env;
    }

    @Override
    public PropData call()  {
        log.info("Call HiveOperatorTask");
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
          //  StepModel sModel = externalStepTypeService.checkStep(task);
            String logic = task.getCommandProfile();

            Map<String,Object> allEtlVars=stringToJsonVar(merge(task.getVars(), task.getLocalResults()));
            log.info("Start hiveServer2 vars:{} {} logic:{}", allEtlVars, System.lineSeparator(), logic);
            logic = getCommand(logic, allEtlVars);
            log.info("Start hiveServer2  {} logic:{}", System.lineSeparator(), logic);

            log.info("logic: {}", logic);

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", logic);
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String taskLog = fullReadStr(reader);

            int processCode = process.waitFor();
            taskLog = taskLog+ System.lineSeparator() + "code result: "+processCode;
            task.addLog(taskLog);
            task.addResultToLocalResult(new ResultTemplate(taskLog));

            try {
                if (processCode == 0){
                    task.setStatus(QueueStatus.SUCCESS.name());
                    task.setStop(OffsetDateTime.now());
                }else{
                    task.setStatus(QueueStatus.FAIL.name());
                    task.setStop(OffsetDateTime.now());
                }
                queueStorageRepository.saveAndFlush(task);
            }catch(Throwable e){
                saveError(task,queueStorageRepository, e, "Error");
            }
            return new PropData(processCode, task, out);

        } catch (IOException | InterruptedException e) {
            saveError(task,queueStorageRepository, e, "Error process");
            return new PropData(-106, task, System.lineSeparator()+ ExceptionUtils.getStackTrace(e) + e.getMessage());
        }
    }

    private String getCommand(String command, Map<String,Object> vars){
        if (!Collections.isEmpty(vars)){
            for (Map.Entry<String, Object> entry: vars.entrySet()){
                String value;
                if(entry.getValue() == null){
                    value ="NOP!";
                }else{
                    value = entry.getValue().toString();
                }
                command = command.replace("{"+entry.getKey()+"}",value);
            }
        }
        return  command;
    }
 }
