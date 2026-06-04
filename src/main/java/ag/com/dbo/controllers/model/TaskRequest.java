package ag.com.dbo.controllers.model;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.Map;

@Data
public class TaskRequest {
    //save
    private String taskId;
    // save
    private String commandProfile;
    // save
    private Map<String, Object> parameters;
    // save
    private String calculateType;
    // save
    private Boolean saveCalculate;

    private Integer maxAttempts = 1;

    private String groovyScript;

}
