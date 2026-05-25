package ag.com.dbo.services.management;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.QueueResult;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepStatus;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class StepInstanceService {

    private final StepInstanceRepository stepInstanceRepository;

    public StepInstanceService(StepInstanceRepository stepInstanceRepository) {
        this.stepInstanceRepository = stepInstanceRepository;
    }

    public void updateStepInstance(QueueResult result){
        Optional<StepInstance> oSi = stepInstanceRepository.findById(result.getTaskId());
        if (oSi.isPresent()){
            StepInstance si = oSi.get();

            if (Objects.equals(result.getStatus(), QueueStatus.SUCCESS.name())) {
                si.setStatus(StepStatus.Success.name());
            }else {
                si.setStatus(StepStatus.Failed.name());
            }
            si.addLog("From queue:"+result.getLog());
            si.setStart(result.getStart());
            si.setStop(result.getStop());
            si.setAttempts(result.getAttempt());
            stepInstanceRepository.save(si);
        }else{
            log.error("{} not found !!!", result.getTaskId());
        }

    }
}
