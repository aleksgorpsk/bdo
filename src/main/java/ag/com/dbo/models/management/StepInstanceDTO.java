package ag.com.dbo.models.management;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class StepInstanceDTO {

    @Id
    private String stepInstanceId;
    private String[] parentStepInstanceIds;
    private String status; // StepStatus
    private EtlInstance etlInstance;
    private Step step;
    private Etl etl;
    private String log;
    private String vars;
    private Integer maxAttempts;
    private Integer attempts;
    private Boolean active;
    private OffsetDateTime start;
    private OffsetDateTime stop;
    private String name;

    /**
     * Add String to log
     * @param message message to add
     * @return return this object
     */
    /*
    public void addLog(String message){
        if (log==null){
            log ="";
        }else{
            log=  log +System.lineSeparator();
        }
        log = log + message;
    }

     */
}