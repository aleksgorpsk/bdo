package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.services.management.ScriptService;
import ag.com.dbo.services.queue.MultithreadExecutor;
import ag.com.dbo.services.queue.QueueService;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.services.queue.model.PropData;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static ag.com.dbo.services.queue.utils.QueueConstants.ERROR_IN_PROCESS;
import static ag.com.dbo.services.queue.utils.QueueConstants.SCRIPT_NOT_FOUND;


@RestController
@RequiredArgsConstructor
@Slf4j
public class QueueController {

    private final QueueService queueService;
    private final MultithreadExecutor multithreadExecutor;
    private final ScriptService scriptService;



    @GetMapping("/queue/info")
        public ResponseEntity<@NonNull QueueInfo> info(){
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type","application/json")
                .body(this.multithreadExecutor.getFreeSpots());
    }


    @PutMapping("/queue/put")
    public ResponseEntity<@NotNull ScriptResponse> enqueue(
            @RequestBody ScriptRequest scriptRequest) throws JsonProcessingException {
        log.info("QueueService :{}", scriptRequest);
        QueueStorage req = new QueueStorage();
        req.setTaskId((scriptRequest.getRequestId()==null)? UUID.randomUUID().toString():  scriptRequest.getRequestId());
        ScriptDefinition definition = scriptRequest.getScriptDefinition();
        Optional<Script> sc = scriptService.getLastScript(definition.getLanguage(), definition.getName());
        if(sc.isEmpty()){
            ScriptResponse resp  = new ScriptResponse();
            resp.setStatus(""+SCRIPT_NOT_FOUND);
            resp.setResponse("");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type","application/json").body(resp);

        }
        Script script = sc.get();

        req.setMaxAttempts((scriptRequest.getMaxAttempts()==null)? 2: scriptRequest.getMaxAttempts());
        req.setStatus(QueueStatus.QUEUE.name());
        req.setStart(OffsetDateTime.now());

        req.setScript(script.getScript());
        req.setScriptType(definition.getType());
        req.setScriptVersion(definition.getVersion());
        req.setScriptLanguage(definition.getLanguage());
        req.setScriptName(definition.getName());

        req.setVars(scriptRequest.getVars());
        req.setLocalResults(scriptRequest.getLocalResults());
        req.setEtlResults(scriptRequest.getEtlResults());
        queueService.save(req);
        runLogic(req);
        ScriptResponse resp= new ScriptResponse();
        resp.setStatus(Constants.OK);
       return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body(resp);
    }

    @PutMapping("/sync/request")
    public ResponseEntity<@NotNull PropData> test(
            @RequestBody ScriptRequest scriptRequest) throws JsonProcessingException {
        log.info("QueueService :{}", scriptRequest);
        QueueStorage req = new QueueStorage();
        req.setTaskId((scriptRequest.getRequestId()==null)? UUID.randomUUID().toString():  scriptRequest.getRequestId());
        ScriptDefinition definition = scriptRequest.getScriptDefinition();
        Optional<Script> sc = scriptService.getLastScript(definition.getLanguage(), definition.getName());
        if(sc.isEmpty()){
            PropData resp  = new PropData();
            resp.setResultStatus(SCRIPT_NOT_FOUND);
            resp.setMessage("Script notfound: "+definition);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type","application/json").body(resp);

        }
        Script script = sc.get();

        req.setMaxAttempts((scriptRequest.getMaxAttempts()==null)? 2: scriptRequest.getMaxAttempts());
        req.setStatus(QueueStatus.QUEUE.name());
        req.setStart(OffsetDateTime.now());

        req.setScript(script.getScript());
        req.setScriptType(definition.getType());
        req.setScriptVersion(definition.getVersion());
        req.setScriptLanguage(definition.getLanguage());
        req.setScriptName(definition.getName());

        req.setVars(scriptRequest.getVars());
        req.setLocalResults(scriptRequest.getLocalResults());
        req.setEtlResults(scriptRequest.getEtlResults());
        queueService.save(req);
        try {
            PropData resultData = multithreadExecutor.syncRun(req);
            return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body(resultData);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Content-Type","application/json").body(new PropData(ERROR_IN_PROCESS, null,e.getMessage()));
        }
    }
    @Async
    private void runLogic(QueueStorage req){
        multithreadExecutor.setRun(req);
    }
}
