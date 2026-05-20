package ag.com.dbo.repositories.queue;

import ag.com.dbo.models.queue.QueueStorage;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QueueStorageRepository extends JpaRepository< @NonNull QueueStorage,  @NonNull String> {

}
