package ag.com.dbo.repositories.queue;

import ag.com.dbo.models.queue.QueueStorage;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;


@Repository
public interface QueueStorageRepository extends JpaRepository< @NonNull QueueStorage,  @NonNull String> {

//QueueStatus
// QUEUE. IN_PROGRES
//    @Query("UPDATE QueueStorage q SET q.status = 'IN_PROGRES' WHERE q.status = 'QUEUE' and q.taskId=:taskId")
//    public void updateStatus(Integer taskId);


    @Query("SELECT q FROM QueueStorage q WHERE q.status = :stat and q.start < :time" )
    List<QueueStorage> findByStatus(@Param("stat") String status,@Param("time") OffsetDateTime time);

}
