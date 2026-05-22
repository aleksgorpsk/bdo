package ag.com.dbo.models.queue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;


@Entity
@Table(name = "queue", schema = "queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStorage {
    @Id
    @Column(name = "taskId", updatable = false)
    private String TaskId;

    private Integer attempt;
    private Integer maxAttempts;

    @Column(columnDefinition = "TEXT")
    private String commandProfile;

    private String dboVersion;  // current :1

    private String status;
    private String calculateType; // processing type

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String log;

    private OffsetDateTime start;
    private OffsetDateTime stop;


}
