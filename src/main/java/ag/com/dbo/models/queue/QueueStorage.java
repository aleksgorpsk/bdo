package ag.com.dbo.models.queue;

import ag.com.dbo.services.queue.utils.VarSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import static io.micrometer.common.docs.KeyName.merge;


@Entity
@Table(name = "queue", schema = "queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStorage {
    @Id
    @Column(name = "taskId", updatable = false)
    private String TaskId;

    private String name;
    private Integer attempt;
    private Integer maxAttempts;

    @Column(columnDefinition = "TEXT")
    private String commandProfile;

    private String dboVersion;  // current :1

    private String status;
    private String calculateType; // processing type

    @Column(columnDefinition = "TEXT")
    private String vars;
    @Column(columnDefinition = "Text")
    private String localResults;

    @Column(columnDefinition = "Text")
    private String etlResults;

    @Column(columnDefinition = "TEXT")
    private String log;

    private OffsetDateTime start;
    private OffsetDateTime stop;


    @Column(columnDefinition = "TEXT")
    private String script;

    private String stepType;

    @Column(columnDefinition = "TEXT")
    private String results;


    /**
     * Add String to log
     * @param message message to add
     * @return
     */
    public void addLog(String message){
        if (log==null){
            log ="";
        }else{
            log=  log +System.lineSeparator();
        }
        log = log + message;
    }

    public String addResultToLocalVar( String name, String value ) throws JsonProcessingException {
        String s= "{  \""+name+"\": \""+value+"\"  } }";
        return VarSupport.merge(getLocalResults(), s );
    }

    public void addResultToLocalResult( Object template ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String s= mapper.writeValueAsString(template);;
        this.setLocalResults(VarSupport.merge(getLocalResults(), s ));
    }

}
