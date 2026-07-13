package ag.com.dbo.controllers.model;

import lombok.Data;


@Data
public class TaskRequest {
    //save
    private String taskId;
    // save
    private String commandProfile;
    // save
    private String vars;

    private String localResult;

    private String etlResult;
    // save
    private String calculateType;
    // save
    private Boolean saveCalculate;

    private Integer maxAttempts = 1;

    private String script;

    private String stepType;

    private String name;

    private String results;

}
