package ag.com.dbo.services.queue;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.management.impl.ExternalServiceImpl;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.services.queue.utils.LoadTaskFactory;
import ag.com.dbo.services.queue.model.PropData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

import static ag.com.dbo.services.queue.utils.QueueConstants.ERROR_IN_PROCESS;

@Slf4j
@Service
public class MultithreadExecutor implements InitializingBean {

    @Value("${queue.executor.thread}")
    private int  threadCount;

    private ThreadPoolExecutor poolExecutor;
    private final Environment env;
    private final QueueStorageRepository queueStorageRepository;
    private final ExternalServiceImpl externalService;

    public MultithreadExecutor(Environment env, QueueStorageRepository queueStorageRepository, ExternalServiceImpl externalService) {
        this.env = env;
        this.queueStorageRepository = queueStorageRepository;
        this.externalService = externalService;
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

    @Async
    public void setRun(QueueStorage request){
        log.info("setRun: {}",request);
        Callable<PropData> task = LoadTaskFactory.getTask(request, env, queueStorageRepository);
        log.info("task!!!: {}",task);
        if (task==null){
            request.addLog("incorrect task name: "+ request.getScriptType());
            request.setStatus(QueueStatus.SYS_ERROR.name());
            queueStorageRepository.saveAndFlush(request);
            sendToManager(request);

        }

        try {
            request.setStatus(QueueStatus.IN_PROGRES.name());
            queueStorageRepository.saveAndFlush(request);
            Future<PropData> fPdata = poolExecutor.submit(task);
            PropData pData = fPdata.get();
            // send response
            log.debug("pData: {}", pData);
            sendToManager(pData.getQs());
        }catch(RejectedExecutionException e) {
            request.setStatus(QueueStatus.QUEUE.name());
            request.addLog("No spot in task! "+e.getMessage());
            queueStorageRepository.saveAndFlush(request);
        } catch (InterruptedException | ExecutionException e) {
            log.error("!!!!Error in task : {0}, {1}", task, e);
            request.setStatus(QueueStatus.QUEUE.name());
            request.addLog("task interrupted !"+e.getMessage());
            int attempt = request.getAttempt() +1;
            request.setAttempt(attempt);
            if (attempt>request.getMaxAttempts()){
                request.setStatus(QueueStatus.SYS_ERROR.name());
            }
            queueStorageRepository.saveAndFlush(request);
            if (attempt>request.getMaxAttempts()) {
                sendToManager(request);
            }
        }
    }

    private void sendToManager(QueueStorage queueStorage){
        try {
            externalService.sendToManager(queueStorage);
        }catch (Exception x){
            queueStorage.addLog("Error to sent to manager:"+x.getMessage());
            queueStorageRepository.saveAndFlush(queueStorage);
        }
    }


    public PropData syncRun(QueueStorage request){
        log.info("SyncRun: {}",request);
        Callable<PropData> task = LoadTaskFactory.getTask(request, env, queueStorageRepository);
        log.info("SyncRun task: {}",task);
        if (task==null){
            request.addLog("incorrect task name: "+ request.getScriptType());
            request.setStatus(QueueStatus.SYS_ERROR.name());
            queueStorageRepository.saveAndFlush(request);
            PropData res= new PropData();
            res.setQs(request);
            res.setMessage("incorrect task name: "+ request.getScriptType());
            res.setResultStatus(ERROR_IN_PROCESS);
            return res;

        }

        try {
            request.setStatus(QueueStatus.IN_PROGRES.name());
            queueStorageRepository.saveAndFlush(request);
            PropData pData = task.call();
            // send response
            log.debug("pData: {}", pData);
            return pData;
        } catch (Exception e) {
            log.error("!!!!Error in task : {0}, {1}", task, e);
            request.setStatus(QueueStatus.FAIL.name());
            request.addLog("task interrupted !"+e.getMessage());
            int attempt = request.getAttempt() +1;
            request.setAttempt(attempt);
            queueStorageRepository.saveAndFlush(request);

            PropData res= new PropData();
            res.setQs(request);
            res.setMessage(e.getMessage());
            res.setResultStatus(ERROR_IN_PROCESS);
            return res;
        }
    }

    /**
     *
     * @return  number of free threads slots and busy threads
     */
    public  QueueInfo getFreeSpots(){
        int poolSize =  poolExecutor.getMaximumPoolSize();
        log.debug("poolSize: {}", poolSize);
        int activeSize = poolExecutor.getActiveCount();
        log.debug("activeSize: {}", activeSize);
        return  new QueueInfo(poolSize - activeSize, activeSize);

    }

}
