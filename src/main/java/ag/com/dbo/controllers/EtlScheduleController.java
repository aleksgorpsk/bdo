package ag.com.dbo.controllers;

import ag.com.dbo.models.management.Etl;
import ag.com.dbo.services.management.EngineService;
import ag.com.dbo.services.management.EtlService;
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

    public EtlScheduleController(EngineService engineService, EtlService etlService) {
        this.engineService = engineService;
        this.etlService = etlService;
    }

    @PutMapping("/schedule")
    public ResponseEntity<String> getAll(@RequestBody ScheduleData request) {
        log.info("schedule:  model{}", request);
        if (request != null) {
            if (request.getEtlId() != null) {
                Optional<Etl> etl = etlService.findEtlById(request.getEtlId());
                if (etl.isPresent()) {
                    engineService.startEtl(etl.get(), true);
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
}
