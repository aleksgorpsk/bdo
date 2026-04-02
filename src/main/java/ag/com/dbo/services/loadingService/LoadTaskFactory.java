package ag.com.dbo.services.loadingService;

import ag.com.dbo.models.StepInstance;
import ag.com.dbo.services.loadingService.model.HiveTask;

import java.util.concurrent.Callable;

public class LoadTaskFactory {

    public static Callable<String>  getTask(StepInstance si) {
        String calculateType = si.getStep().getCalculateMethod();
        if (calculateType.equals(TaskName.CSV_TO_HIVE.name())) {
            return new HiveTask(si);
        }
        return null;
    }
}
