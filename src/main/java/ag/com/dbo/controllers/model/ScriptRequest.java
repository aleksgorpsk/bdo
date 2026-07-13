package ag.com.dbo.controllers.model;

import lombok.Data;

@Data
public class ScriptRequest {
    private String language;
    private String scriptName;
    private String version;
    private String stepName;
    private String vars;
    private String resultName;
    private String localResults;
    private String etlResults;
}
