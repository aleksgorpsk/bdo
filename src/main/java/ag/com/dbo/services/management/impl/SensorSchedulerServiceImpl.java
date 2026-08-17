package ag.com.dbo.services.management.impl;

import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepStatus;
import ag.com.dbo.services.management.SensorSchedulerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ag.com.dbo.services.Utils.getSensor;


@Slf4j
@Service
public class SensorSchedulerServiceImpl implements SensorSchedulerService {

    private final StepInstanceServiceImpl stepInstanceService;
    private final EngineServiceImpl engineService;

    @Value("${sensor.default.timeout}")
    private Long sensorDefaultTimeout;

    public SensorSchedulerServiceImpl(StepInstanceServiceImpl stepInstanceService, EngineServiceImpl engineService) {
        this.stepInstanceService = stepInstanceService;
        this.engineService = engineService;
    }


    @Scheduled(fixedRateString = "${sensor.scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
    public void scheduling(){
        log.debug("sensor scheduled start !!!");
        List<StepInstance> result = stepInstanceService.getActiveSensors().stream().toList();
        log.info("sensor scheduled !!!:{}" ,result.size());
        List<StepInstance> updatedTimeout = result.stream().map(this::addTimeOut).toList();
        List<StepInstance> updatedProcess = stepInstanceService.save(updatedTimeout);
        List<StepInstance> readyToProcess = updatedProcess
                .stream().filter(x-> !StepStatus.Failed.name().equals(x.getStatus())).toList();
        readyToProcess.forEach(x-> engineService.enqueueTask(x, null));
    }



    private  StepInstance addTimeOut(StepInstance si) {
        try {
            CommonModel sModel = getSensor(si.getVars());
            Long timeout = sModel.getAttemptTimeOut()==null?sensorDefaultTimeout:sModel.getAttemptTimeOut();
            si.setNextTest(si.getNextTest()+timeout);

            OffsetDateTime odt = Instant.ofEpochSecond(si.getNextTest()).atOffset(ZoneOffset.UTC);
            if ((Instant.now().getEpochSecond()-si.getStart().toEpochSecond()) > sModel.getFailTimeout()){
                si.addLog(" ERROR: Timeout in sensor: timeout: "+sModel.getFailTimeout()+" at:" +OffsetDateTime.now());
                si.setStatus(StepStatus.Failed.name());
            }else{
                si.addLog(" Test sensor:"+si.getName()+" timeout: "+sModel.getFailTimeout()+" at:" +odt);
            }
            return si;

        } catch (JsonProcessingException e) {
            String message = " No timeout in "+si.getName() +":"+si.getStepInstanceId() +" : " +si.getVars() +" "+ e.getMessage();
            log.error(message);
            si.addLog(message);
            si.setStatus(StepStatus.Failed.name());
        }
        return si;
    }

}
