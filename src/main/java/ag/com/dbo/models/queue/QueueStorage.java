package ag.com.dbo.models.queue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "queue", schema = "queue" )
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

//    private String result;
    private String dboVersion;  // current :1
//    private String resultCode;
    private String status;
    private String calculateType; // processing type
//    private Integer repetitions;

    @Column(columnDefinition = "TEXT")
    private String parameters;
    @Column(columnDefinition = "TEXT")
    private String log;


    private LocalDateTime start;
    private LocalDateTime stop;


}
