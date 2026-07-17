package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.services.queue.MultithreadExecutor;
import ag.com.dbo.services.queue.QueueService;
import ag.com.dbo.models.management.statuses.QueueInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@RestController
@Slf4j
public class QueueController {

    private final QueueService queueService;
    private final MultithreadExecutor multithreadExecutor;


    public QueueController(QueueService queueService, MultithreadExecutor multithreadExecutor) {
        this.queueService = queueService;
        this.multithreadExecutor = multithreadExecutor;
    }

    @GetMapping("/queue/info")
        public ResponseEntity<@NonNull QueueInfo> info(){
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type","application/json")
                .body(this.multithreadExecutor.getFreeSpots());
    }


    @PutMapping("/queue/put")
    public ResponseEntity<@NotNull QueueStorage> enqueue(
            @RequestBody TaskRequest taskRequest) throws JsonProcessingException {
        log.info("QueueService :{}", taskRequest);
        QueueStorage req = new QueueStorage();
        String taskId= (taskRequest.getTaskId()==null)? UUID.randomUUID().toString():  taskRequest.getTaskId();
        req.setTaskId(taskId);

        req.setCommandProfile(taskRequest.getCommandProfile());
        req.setName(taskRequest.getName());
        req.setCalculateType(taskRequest.getCalculateType());
        req.setMaxAttempts((taskRequest.getMaxAttempts()==null)? 2: taskRequest.getMaxAttempts());
        req.setStatus(QueueStatus.QUEUE.name());
        req.setStart(OffsetDateTime.now());
        req.setScript(taskRequest.getScript());
        req.setStepType(taskRequest.getStepType());
        req.setVars(taskRequest.getVars());
        req.setLocalResults(taskRequest.getLocalResult());
        req.setEtlResults(taskRequest.getEtlResult());
        req.setResults(taskRequest.getResults());
        queueService.save(req);
        runLogic(req);
       return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body(req);
    }

    @Async
    private void runLogic(QueueStorage req){
        multithreadExecutor.setRun(req);
    }
}
