package ag.com.dbo.services.queue.model;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.TaskProperties;
import ag.com.dbo.utils.Utils;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static ag.com.dbo.services.queue.utils.LogParser.parseTableAndSearchData;
import static ag.com.dbo.services.queue.utils.VarSupport.merge;

@Slf4j
public class HiveOperatorTask extends TaskProperties implements Callable<PropData> {
    /**
     *  errors:
     *  -105 : too many attempts
     *  -106 : process error
     */
    private final QueueStorage task;
    private String out;
    private final  Environment env;
    private final QueueStorageRepository queueStorageRepository;

    public HiveOperatorTask(QueueStorage data, Environment env, QueueStorageRepository queueStorageRepository){
        super(env);
        this.queueStorageRepository = queueStorageRepository;
        task = data;
        this.env = env;
    }

    @Override
    public PropData call() throws Exception {
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

            String sVars=task.getParameters();
            Map<String, Object > vars = new HashMap<>();
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
                for (Map.Entry<String, Object> entry: vars.entrySet()){
                    logic = logic.replace("{"+entry.getKey()+"}",entry.getValue().toString());
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


            String taskLog = fullReadStr(reader);
            int processCode = process.waitFor();
            taskLog = taskLog+ System.lineSeparator()+ " code result: "+processCode;

            task.addLog(taskLog);
            String result=null;
            if (Boolean.TRUE.equals(task.getSaveCalculate())) {
                result = getResult(taskLog, vars);
                log.info("result:{}", result);
            }
            try {
                if (Boolean.TRUE.equals(task.getSaveCalculate())) {
                    task.setEtlVars(merge(task.getEtlVars(), result));
                }
                if (processCode == 0){
                    task.setStatus(QueueStatus.SUCCESS.name());
                }else{
                    task.setStatus(QueueStatus.FAIL.name());
                }
            }catch(Throwable e){
                log.error("Error !", e);
                task.setStatus(QueueStatus.FAIL.name());
            }

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

    /**
     *
     * @param fullOut full out data
     * @param vars vars must have rowNum (0 by default) and vat with name columnName
     * @return value
     */
    private String getResult(String fullOut, Map<String, Object > vars){
       int start = fullOut.indexOf("+-");
        int stop = fullOut.lastIndexOf("-+");
        String sRowNum= vars.getOrDefault("rowNum", "0").toString();
        int rowNum =0;
        List<String> columnName = (List<String>) vars.getOrDefault("columnName", List.of());
        try{
            rowNum = Integer.parseInt(sRowNum);
        }catch(NumberFormatException e){
            task.addLog("Incorrect sRowNum . Must be int or not present:" + sRowNum);
            return null;
        }

        if(start ==-1 || stop==-1 || columnName==null){
            log.error("No response in log");
            task.addLog("".repeat(20));
            task.addLog("No data for parse response: start('+-'):"+start+", stop('-+'):"+stop+", columnName:" + columnName);
            return null;
        }
        String table= fullOut.substring(start, stop+2);
        try{
            return parseTableAndSearchData(table, rowNum, columnName);
        }catch(Exception e){
            task.addLog("Incorrect parsing result data! "+e.getMessage());
            return null;
        }
    }

    private String getNamedResult(String fullOut, Map<String, Object > vars){
        String rawResult = getResult(fullOut, vars);
        String columnName = vars.get("columnName").toString();

        return columnName;

    }
}
