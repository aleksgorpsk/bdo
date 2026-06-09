package ag.com.dbo.services.queue.utils;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.model.*;
import org.springframework.core.env.Environment;

import java.util.concurrent.Callable;

public class LoadTaskFactory {

    public static Callable<PropData> getTask(QueueStorage task , Environment env, QueueStorageRepository queueStorageRepository) {
        String calculateType = task.getCalculateType();
        if (TaskName.CSV_TO_HIVE.name().equals(calculateType)) {
            return new HiveTask(task, env, queueStorageRepository);
        }
        if (TaskName.HIVE_TO_DB.name().equals(calculateType)) {
            return new HiveToJdbcTask(task, env, queueStorageRepository);
        }
        if (TaskName.SIMPLE.name().equals(calculateType)) {
            return new SimpleBashTask(task, env, queueStorageRepository);
        }
        if (TaskName.HIVEOPERATOR.name().equals(calculateType)) {
            return new HiveOperatorTask(task, env, queueStorageRepository);
        }

        return null;
    }
}
