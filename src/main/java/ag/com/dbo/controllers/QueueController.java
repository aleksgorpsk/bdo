package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.services.queue.MultithreadExecutor;
import ag.com.dbo.services.queue.QueueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@RestController
//@RequestMapping("/queue")
@Slf4j
public class QueueController {

    private final QueueService queueService;
    private final MultithreadExecutor multithreadExecutor;


    public QueueController(QueueService queueService, MultithreadExecutor multithreadExecutor) {
        this.queueService = queueService;
        this.multithreadExecutor = multithreadExecutor;
    }

    @PutMapping("/queue/put")
    public ResponseEntity<@Nullable QueueStorage> enqueue(
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
        req.setSaveCalculate( taskRequest.getSaveCalculate());
        req.setGroovyScript(taskRequest.getGroovyScript());
        req.setStepType(taskRequest.getStepType());
        req.setParameters(taskRequest.getParameters());
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
