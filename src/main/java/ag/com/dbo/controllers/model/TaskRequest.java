package ag.com.dbo.controllers.model;

import lombok.Data;


@Data
public class TaskRequest {
    //save
    private String taskId;
    // save
    private String commandProfile;
    // save
    private String parameters;
    // save
    private String calculateType;
    // save
    private Boolean saveCalculate;

    private Integer maxAttempts = 1;

    private String groovyScript;

}
