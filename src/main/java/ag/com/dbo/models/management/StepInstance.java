package ag.com.dbo.models.management;

import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.checker.ModelParser;
import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.script.ScriptDefinition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import jakarta.persistence.*;


@Entity
@Table(name = "step_instance", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class StepInstance {

    @Id
    @Column(columnDefinition = "varchar(40)", name = "step_instance_id", updatable = false)
    private String stepInstanceId;

    @Column( name = "parent_Step_Instance_Ids")
    private String[] parentStepInstanceIds;

//    @Transient
//    private String[] activeParentStepInstanceIds;

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
    private String logMessage;
    private Integer maxAttempts;
    private Integer attempts;
    private Boolean active;
//timestamp with time zone
    @Column( columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime start;
    @Column( columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime stop;
    private String name;


    @Column(columnDefinition = "Text")
    private String vars;  // step result

    @Column(columnDefinition = "Text")
    private String localResults;  // vars

//    @Column(columnDefinition = "TEXT")
//    private String script; // comma separated scriptLink

    @Column(columnDefinition = "TEXT")
    private String stepType; // comma separated type (maybe many types)

    private Long nextTest; // UTC Epoch
    private String tags; // comma separated

    @Transient
    private ModelParser scriptModelFactory;

    public CommonModel getScriptModel(ModelName type){
        if (scriptModelFactory==null){
            scriptModelFactory = new ModelParser(this.getVars());
        }
        return scriptModelFactory.getModel(type);
    }
    public CommonModel getModelByScriptDefinition(ScriptDefinition scriptDefinition){
        if (scriptModelFactory==null){
            scriptModelFactory = new ModelParser(this.getVars());
        }
        return scriptModelFactory.getModelByScriptDefinition(scriptDefinition);
    }

    public boolean isSensor(){
        return getScriptModel(ModelName.sensorScript)!=null;
    }
    public boolean isShellCommand(ScriptDefinition scriptDefinition){
        CommonModel scriptModel = getScriptModel(ModelName.shellCommandScript);
        if(scriptDefinition == null || scriptModel == null){
            return false;
        }
        return scriptDefinition.equals(scriptModel.getScriptDefinition());
    }

    /**
     * Add String to log
     * @param message message to add
     * @return return this object
     */

    public void addLog(String message){
        log.info("{}-{}", OffsetDateTime.now(), message);

        if (logMessage==null){
            logMessage ="";
        }
        logMessage = logMessage+ "\n" + OffsetDateTime.now()+" - " + message;
    }
}