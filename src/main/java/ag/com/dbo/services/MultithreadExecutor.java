package ag.com.dbo.services;

import ag.com.dbo.services.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
public class MultithreadExecutor implements InitializingBean {

    @Value("${executor.thread}")
    private int  threadCount;

    private ExecutorService poolExecutor;


    public void afterPropertiesSet() {
        log.info("!! afterPropertiesSet2 !");
        this.poolExecutor = Executors.newFixedThreadPool(this.threadCount);

    };

    public String exec(String s){
        log.info(s+"start:");
        long start= System.currentTimeMillis();
        Callable<String> task = new Task(s);
        Future<String> future = poolExecutor.submit(task);
        try {
            String result = future.get();
            log.info(s +" finish:"+(System.currentTimeMillis()-start));
            return result;

        } catch (InterruptedException | ExecutionException e) {
            log.info(s +" error finish:"+(System.currentTimeMillis()-start));
            return  "s:"+ e.toString();

        }
    }
}
