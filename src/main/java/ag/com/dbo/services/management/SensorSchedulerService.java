package ag.com.dbo.services.management;

import ag.com.dbo.models.management.StepInstance;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SensorSchedulerService {

    private final StepInstanceService stepInstanceService;
    private final EngineService engineService;

    public SensorSchedulerService(StepInstanceService stepInstanceService, EngineService engineService) {
        this.stepInstanceService = stepInstanceService;
        this.engineService = engineService;
    }


    @Scheduled(fixedRateString = "${sensor.scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void scheduling(){
        List<StepInstance> result =  stepInstanceService.getActiveSensors();
        log.debug("sensor scheduled !!!:{}",result);
        result.forEach(x-> engineService.enqueueTask(x, null));
    }

}
