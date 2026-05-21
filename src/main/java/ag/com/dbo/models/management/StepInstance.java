package ag.com.dbo.models.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "step_instance", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepInstance {

    @Id
    @Column(columnDefinition = "varchar(40)", name = "step_instance_id", updatable = false)
    private String stepInstanceId;

    @Column( name = "parent_Step_Instance_Ids")
    private String[] parentStepInstanceIds;

    private String status; // StepStatus

    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "etl_instance_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private EtlInstance etlInstance;

    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "step_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private Step step;

    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private Etl etl;

    @Column(columnDefinition = "Text")
    private String log;
    @Column(columnDefinition = "Text")
    private String vars;
    private Integer maxAttempts;
    private Integer attempts;
    private Boolean active;
    private LocalDateTime start;
    private LocalDateTime stop;

}