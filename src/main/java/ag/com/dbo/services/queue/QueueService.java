package ag.com.dbo.services.queue;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.models.management.statuses.QueueInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class QueueService {

    private final QueueStorageRepository queueStorageRepository;
    private final MultithreadExecutor multithreadExecutor;


    @Scheduled(fixedRateString = "${queue.scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
    private void checkQueue(){
        OffsetDateTime borderTime =OffsetDateTime.now().minusSeconds(3);
        int free = multithreadExecutor.getFreeSpots().getFreeSlots();
        List<QueueStorage> tasks = queueStorageRepository.findByStatus(QueueStatus.QUEUE.name(), borderTime).stream().limit(free).toList();
        log.info("List<QueueStorage>:{} free :{}", tasks.size(), free );
        tasks.forEach(multithreadExecutor::setRun);

    }

    public QueueInfo getStatus(){
        OffsetDateTime borderTime =OffsetDateTime.now().minusSeconds(3);
        QueueInfo info = multithreadExecutor.getFreeSpots();
        List<QueueStorage> tasks = queueStorageRepository.findByStatus(QueueStatus.QUEUE.name(), borderTime).stream().limit(info.getFreeSlots()).toList();
        log.info("QueueInfo:{} ", info );
        return info;
    }

    public QueueService(QueueStorageRepository queueStorageRepository, MultithreadExecutor multithreadExecutor) {
        this.queueStorageRepository = queueStorageRepository;
        this.multithreadExecutor = multithreadExecutor;
    }

    public QueueStorage save(QueueStorage queue){
        return queueStorageRepository.saveAndFlush(queue);
    }

    public QueueStorage enqueue(QueueStorage task){
        return new QueueStorage();
    }
}
