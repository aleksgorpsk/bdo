package ag.com.dbo.services.queue.utils;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.model.*;
import org.springframework.core.env.Environment;

import java.util.concurrent.Callable;

public class LoadTaskFactory {

    public static Callable<PropData> getTask(QueueStorage task , Environment env,
                                             QueueStorageRepository queueStorageRepository                                             ) {
        String calculateType = task.getScriptType();
        if (TaskName.HIVEOPERATOR.name().equals(calculateType)) {
            return new HiveOperatorTask(task, env, queueStorageRepository);
        }
        if (TaskName.ShellCommand.name().equals(calculateType)) {
            return new HiveOperatorTask(task, env, queueStorageRepository);
        }


        return null;
    }
}
