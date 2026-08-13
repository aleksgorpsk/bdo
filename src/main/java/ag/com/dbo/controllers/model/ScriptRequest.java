package ag.com.dbo.controllers.model;

import ag.com.dbo.models.checker.varmodel.CommonModel;
import lombok.Data;

@Data
public class ScriptRequest {
    private CommonModel commonModel;
    private String requestId;
    private String stepName;
    // TODO ????
    private String resultName;

    private String vars;
    private String localResults;
    private String etlResults;

    private String tags;
}
