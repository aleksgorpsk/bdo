package ag.com.dbo.models.management;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "queue", schema = "queue" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueResult {
    @Id
    @Column(name = "taskId", updatable = false)
    private String TaskId;

    private Integer attempt;

    private String dboVersion = "1";  // current :1
    private String status;

    @Column(columnDefinition = "TEXT")
    private String log;

    private LocalDateTime start;
    private LocalDateTime stop;


}
