package ag.com.dbo.services.queue;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
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
    private final ManagerService managerService;
    private final Environment env;
    private final QueueStorageRepository queueStorageRepository;

    public MultithreadExecutor(ManagerService managerService, Environment env, QueueStorageRepository queueStorageRepository) {
        this.managerService = managerService;
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
        log.info("setRun: {}",request);
        Callable<PropData> task = LoadTaskFactory.getTask(request, env, queueStorageRepository);
        Future <PropData> fPdata = poolExecutor.submit(task);
        try {
            PropData pData = fPdata.get();
            // send response
            log.info("pData: {}", pData);
            managerService.sendResult(pData.getQs());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error in task : {0}, {1}", task, e);
            request.setStatus(QueueStatus.SYS_ERROR.name());
            queueStorageRepository.saveAndFlush(request);
            managerService.sendResult(request);

        }
    }

}
