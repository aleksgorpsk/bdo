package ag.com.dbo.services.loadingService;

import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
import ag.com.dbo.services.loadingService.model.HiveTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
public class MultithreadExecutor implements InitializingBean {

    @Value("${executor.thread}")
    private int  threadCount;

    private ExecutorService poolExecutor;
    private final Environment env;
    private final StepInstanceRepository stepInstanceRepository;

    public MultithreadExecutor(Environment env, StepInstanceRepository stepInstanceRepository) {
        this.env = env;
        this.stepInstanceRepository = stepInstanceRepository;
    }

    public void afterPropertiesSet() {
        log.info("!! afterPropertiesSet2 !");
        this.poolExecutor = Executors.newFixedThreadPool(this.threadCount);

    };

    public String exec(StepInstance si){
        log.info("start:"+si);
        long start= System.currentTimeMillis();
        Callable<String> task = LoadTaskFactory.getTask(si, env, stepInstanceRepository);
        Future<String> future = poolExecutor.submit(task);
        try {
            String result = future.get();
            log.info(si +" finish:"+(System.currentTimeMillis()-start));
            return result;

        } catch (InterruptedException | ExecutionException e) {
            log.info(si +" error finish:"+(System.currentTimeMillis()-start));
            return  "s:"+ e.toString();

        }
    }

}
