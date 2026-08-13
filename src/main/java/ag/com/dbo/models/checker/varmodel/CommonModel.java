package ag.com.dbo.models.checker.varmodel;

import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.script.ScriptDefinition;
import lombok.Data;

@Data
public class CommonModel {
    private String scriptName;//": "GROOVY:test1:12",
    private ScriptDefinition scriptDefinition;
    private ModelName modelName;
    private Long failTimeout;
    private Long timeout;
    private String resultName;
    private Long attemptTimeOut;
    private Integer maxAttempts;
}
