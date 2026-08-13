package ag.com.dbo.models.management;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

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
//    private Integer maxAttempts;
    private Integer attempts;
    private Boolean active;
    private OffsetDateTime start;
    private OffsetDateTime stop;
    private String name;

    private String etlVars;

    private OffsetDateTime nextTest;

    private String vars;  // step result

    private String localResults;  // vars
    private String logMessage;  // vars

    private List<String> scriptList;

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