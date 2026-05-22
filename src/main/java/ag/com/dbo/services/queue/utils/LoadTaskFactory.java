package ag.com.dbo.services.queue.utils;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.model.HiveToJdbcTask;
import ag.com.dbo.services.queue.model.HiveTask;
import ag.com.dbo.services.queue.model.PropData;
import ag.com.dbo.services.queue.model.SimpleBashTask;
import org.springframework.core.env.Environment;

import java.util.concurrent.Callable;

public class LoadTaskFactory {

    public static Callable<PropData> getTask(QueueStorage task , Environment env, QueueStorageRepository queueStorageRepository) {
        String calculateType = task.getCalculateType();
        if (calculateType.equals(TaskName.CSV_TO_HIVE.name())) {
            return new HiveTask(task, env, queueStorageRepository);
        }
        if (calculateType.equals(TaskName.HIVE_TO_DB.name())) {
            return new HiveToJdbcTask(task, env, queueStorageRepository);
        }
        if (calculateType.equals(TaskName.MESSAGE.name())) {
            return new SimpleBashTask(task, env, queueStorageRepository);
        }
        return null;
    }
}
