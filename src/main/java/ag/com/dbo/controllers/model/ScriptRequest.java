package ag.com.dbo.controllers.model;

import ag.com.dbo.models.script.ScriptDefinition;
import lombok.Data;

@Data
public class ScriptRequest {
    private ScriptDefinition scriptDefinition;
    private String stepName;
    private String vars;
    private String resultName;
    private String localResults;
    private String etlResults;
}
