package ag.com.dbo.services.loadingService;

import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
import ag.com.dbo.services.loadingService.model.HiveTask;
import org.springframework.core.env.Environment;

import java.util.concurrent.Callable;

public class LoadTaskFactory {

    public static Callable<String> getTask(StepInstance si , Environment env, StepInstanceRepository stepInstanceRepository) {
        String calculateType = si.getStep().getCalculateMethod();
        if (calculateType.equals(TaskName.CSV_TO_HIVE.name())) {
            return new HiveTask<>(si, env, stepInstanceRepository);
        }
        return null;
    }
}
