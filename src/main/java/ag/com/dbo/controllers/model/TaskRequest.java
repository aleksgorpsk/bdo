package ag.com.dbo.controllers.model;

import lombok.Data;

import java.util.Map;

@Data
public class TaskRequest {

    //save
    private String taskId;
    // save
    private String commandProfile;
    // save
    private Map<String, String> parameters;
    // save
    private String calculateType;

    private Integer maxAttempts = 1;


}
