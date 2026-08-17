package ag.com.dbo.controllers;

import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.services.management.impl.EngineServiceImpl;
import ag.com.dbo.services.management.impl.StepInstanceServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class ManagementController {

    private final StepInstanceServiceImpl stepInstanceService;
    private final EngineServiceImpl engineService;

    public ManagementController(StepInstanceServiceImpl stepInstanceService, EngineServiceImpl engineService) {
        this.stepInstanceService = stepInstanceService;
        this.engineService = engineService;
    }

    @PutMapping("/return/task")
    public ResponseEntity<@Nullable String> returnTask(@RequestBody QueueStorage queueResult) {
        log.info("Manager returnTask: {}", queueResult);
        try {
            StepInstance si =stepInstanceService.updateStepInstance(queueResult);
            engineService.stepFrom(si);
        }catch (Exception e){
            log.error("Error "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Content-Type","application/json").body("Error: "+e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body("OK");
    }


}
