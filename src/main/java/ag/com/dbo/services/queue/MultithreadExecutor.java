package ag.com.dbo.services.queue;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.utils.LoadTaskFactory;
import ag.com.dbo.services.queue.model.PropData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
public class MultithreadExecutor implements InitializingBean {

    @Value("${queue.executor.thread}")
    private int  threadCount;

    private ThreadPoolExecutor poolExecutor;
    private final Environment env;
    private final QueueStorageRepository queueStorageRepository;

    public MultithreadExecutor(Environment env, QueueStorageRepository queueStorageRepository) {
        this.env = env;

        this.queueStorageRepository = queueStorageRepository;
    }

    public void afterPropertiesSet() {
        log.info("afterPropertiesSet2 !");

        this.poolExecutor = new ThreadPoolExecutor(
                threadCount,
                threadCount,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy()
        );

    };

    public void setRun(QueueStorage request){
        Callable<PropData> task = LoadTaskFactory.getTask(request, env, queueStorageRepository);
        poolExecutor.submit(task);
    }
/*
    public PropData exec(StepInstance si){
        log.info("start:"+si);
        long start= System.currentTimeMillis();
        Callable<PropData> task = LoadTaskFactory.getTask(si, env, stepInstanceRepository);
        Future<PropData> future = poolExecutor.submit(task);
        try {
            PropData result = future.get();
            log.info("{} finish: {}", si, (System.currentTimeMillis()-start));
            return result;
        } catch (InterruptedException | ExecutionException e) {
            log.info("{} error finish: {}", si, (System.currentTimeMillis()-start));
            return  new PropData(-99,"s:"+ e.toString());
        }
    }
*/
}
