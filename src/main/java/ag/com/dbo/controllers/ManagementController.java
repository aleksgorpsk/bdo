package ag.com.dbo.controllers;

import ag.com.dbo.models.management.QueueResult;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.services.EngineService;
import ag.com.dbo.services.management.StepInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class ManagementController {

    private final StepInstanceService stepInstanceService;
    private final EngineService engineService;

    public ManagementController(StepInstanceService stepInstanceService, EngineService engineService) {
        this.stepInstanceService = stepInstanceService;
        this.engineService = engineService;
    }

    @PutMapping("/return/task")
    public ResponseEntity<@Nullable String> returnTask(@RequestBody QueueResult queueResult) {
        log.info("Manager returnTask: {}", queueResult);
        stepInstanceService.updateStepInstance(queueResult);
        log.info("msg:{}",queueResult);
        engineService.stepFrom(queueResult.getTaskId());
        return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body("OK");
    }


}
