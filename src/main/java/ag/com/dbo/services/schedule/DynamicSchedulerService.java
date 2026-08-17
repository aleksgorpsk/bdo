package ag.com.dbo.services.schedule;


import ag.com.dbo.models.management.Etl;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.services.management.impl.ExternalServiceImpl;
import ag.com.dbo.services.management.SendUrlTask;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class DynamicSchedulerService   {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<BigInteger, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final EtlRepository etlRepository;
    private final ExternalServiceImpl externalService;

    @PostConstruct
    public void init() {
        List<Etl> etls=etlRepository.findByActive();
//        etls.forEach(x-> scheduleTask(x.getId(),x.getCronScheduling()));
        log.info("!!");
    }

    public void scheduleTask(BigInteger taskId, String cronExpression) {

        cancelTask(taskId);

        ScheduledFuture<?> future = taskScheduler
                .schedule(new SendUrlTask(externalService, taskId), new CronTrigger(cronExpression) );

        scheduledTasks.put(taskId, future);
    }


    // Метод для отмены задачи
    public void cancelTask(BigInteger taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            future.cancel(true);
            scheduledTasks.remove(taskId);
        }
    }
}
