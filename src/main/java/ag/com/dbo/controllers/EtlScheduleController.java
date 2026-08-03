package ag.com.dbo.controllers;

import ag.com.dbo.models.management.Etl;
import ag.com.dbo.services.management.EngineService;
import ag.com.dbo.services.management.EtlService;
import ag.com.dbo.services.schedule.DynamicSchedulerService;
import ag.com.dbo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ag.com.scheduling.models.ScheduleData;

import java.util.Optional;

@Slf4j
@Controller
public class EtlScheduleController {

    private final EngineService engineService;
    private final EtlService etlService;
    private final DynamicSchedulerService dynamicSchedulerService;

    public EtlScheduleController(EngineService engineService, EtlService etlService, DynamicSchedulerService dynamicSchedulerService) {
        this.engineService = engineService;
        this.etlService = etlService;
        this.dynamicSchedulerService = dynamicSchedulerService;
    }

    @PutMapping("/schedule/delete")
    public ResponseEntity<String> cancelTask(@RequestBody ScheduleData request) {
        log.info("schedule:  model{}", request);
        if (request != null) {
            if (request.getEtlId() != null) {
                Optional<Etl> etl = etlService.findEtlById(request.getEtlId());
                if (etl.isPresent()) {
                    dynamicSchedulerService.cancelTask(etl.get().getId());
                    return ResponseEntity.status(HttpStatus.OK).body(Constants.OK);
                }else{
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Constants.ERROR);
                }
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.ERROR);
            }
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.ERROR);
        }
    }
    
    @PutMapping("/schedule/update")
    public ResponseEntity<String> updateTask(@RequestBody Etl request) {
        log.info("updateTask schedule: model{}", request);
        if (request != null) {
            dynamicSchedulerService.scheduleTask(request.getId(), request.getCronScheduling());
            return ResponseEntity.status(HttpStatus.OK).body(Constants.OK);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.ERROR);
        }
    }

}
