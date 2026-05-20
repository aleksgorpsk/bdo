package ag.com.dbo.services.queue;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private final QueueStorageRepository queueStorageRepository;

    public QueueService(QueueStorageRepository queueStorageRepository) {
        this.queueStorageRepository = queueStorageRepository;
    }

    public QueueStorage save(QueueStorage queue){
        return queueStorageRepository.save(queue);
    }

    public QueueStorage enqueue(QueueStorage task){
        return new QueueStorage();
    }
}
