package ag.com.dbo.models.checker.script;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptInfo {
    private String scriptName;
    private String resultName;
    private Long attemptTimeOut; // sec
    private Long failTimeout;
}
