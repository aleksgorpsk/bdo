package ag.com.dbo.models.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "etl_instance_id", nullable = false) // Specifies the FK column name
    private EtlInstance etlInstance;

    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "step_id", nullable = false) // Specifies the FK column name
    private Step step;

    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false) // Specifies the FK column name
    private Etl etl;

    @Column(columnDefinition = "Text")
    private String log;
    @Column(columnDefinition = "Text")
    private String vars;
    private Integer maxAttempts;
    private Integer attempts;
    private Boolean active;
    private OffsetDateTime start;
    private OffsetDateTime stop;
    /**
     * Add String to log
     * @param message message to add
     * @return return this object
     */
    public void addLog(String message){
        if (log==null){
            log ="";
        }else{
            log=  log +System.lineSeparator();
        }
        log = log + message;
    }
}