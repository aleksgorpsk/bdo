package ag.com.dbo.services.queue.utils;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.management.ExternalStepTypeService;
import ag.com.dbo.services.queue.model.*;
import org.springframework.core.env.Environment;

import java.util.concurrent.Callable;

public class LoadTaskFactory {

    public static Callable<PropData> getTask(QueueStorage task , Environment env,
                                             QueueStorageRepository queueStorageRepository,
                                             ExternalStepTypeService externalStepTypeService) {
        String calculateType = task.getCalculateType();
        if (TaskName.HIVEOPERATOR.name().equals(calculateType)) {
            return new HiveOperatorTask(task, env, queueStorageRepository, externalStepTypeService);
        }

        return null;
    }
}
