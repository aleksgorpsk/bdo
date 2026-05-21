package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.models.queue.TaskParameters;
import ag.com.dbo.services.queue.MultithreadExecutor;
import ag.com.dbo.services.queue.QueueService;
import ag.com.dbo.services.queue.TaskProperties;
import ag.com.dbo.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        // Demo validation (replace with DB check in real apps)
        QueueStorage req = new QueueStorage();
        String taskId= (taskRequest.getTaskId()==null)? UUID.randomUUID().toString():  taskRequest.getTaskId();
        req.setTaskId(taskId);

        req.setCommandProfile(taskRequest.getCommandProfile());
        if (taskRequest.getParameters() !=null) {
            req.setParameters(Utils.objectToString(taskRequest.getParameters()));
        }
        req.setCalculateType(taskRequest.getCalculateType());
        req.setMaxAttempts(taskRequest.getMaxAttempts());
        req.setStatus(QueueStatus.QUEUS.name());
        queueService.save(req);
        multithreadExecutor.setRun(req);
       return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body(req);
    }

}
