package ag.com.dbo.controllers.model;

import lombok.Data;

@Data
public class ScriptRequest {
    private String language;
    private String scriptName;
    private Integer version;
    private String results;
    private String params;
    private String stepName;
}
