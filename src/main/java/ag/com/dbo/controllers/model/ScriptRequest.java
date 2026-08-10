package ag.com.dbo.controllers.model;

import ag.com.dbo.models.script.ScriptDefinition;
import lombok.Data;

@Data
public class ScriptRequest {
    private ScriptDefinition scriptDefinition;
    private String requestId;
    private String stepName;
    // TODO ????
    private String resultName;

    private String vars;
    private String localResults;
    private String etlResults;

    private String tags;
    private Integer maxAttempts;
}
