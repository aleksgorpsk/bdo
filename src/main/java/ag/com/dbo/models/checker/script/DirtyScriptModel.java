package ag.com.dbo.models.checker.script;

import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
/**
 * full name = Language:name[:version]
 *
 *  {
 * "directoryName":"/Users/aleksgor/opt/data"
 * "scripts":[{
 * "name:": "testBranch3",
 * "resultName":"result1"
 * }
 * ]
 * }
 */
public class DirtyScriptModel {
    private Long attemptTimeOut; // sec
    private Long failTimeout;
    private String resultName;

    private List<ScriptInfo> scripts;


    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

    public ScriptModel getScriptModel(String scriptName) {
        if (StringUtils.isEmpty(scriptName)) {
            return null;
        }
        ScriptModel result = new ScriptModel();
        List<ScriptInfo> resultInfo = scripts.stream().filter(x -> scriptName.equals(x.getScriptName())).toList();
        ScriptInfo resultInfoForScript = null;
        if (!resultInfo.isEmpty()) {
            resultInfoForScript = resultInfo.get(0);
        }
        if (resultInfoForScript == null) {
            result.setResultName(this.getResultName());
            result.setAttemptTimeOut(this.getAttemptTimeOut());
            result.setFailTimeout(this.getFailTimeout());
        } else {
            if (StringUtils.isEmpty(resultInfoForScript.getResultName())) {
                if (StringUtils.isEmpty(this.getResultName())){
                    result.setResultName(Constants.SCRIPT_RESULT);
                }else{
                    result.setResultName(this.getResultName());
                }
            } else {
                result.setResultName(resultInfoForScript.getResultName());
            }

            if (resultInfoForScript.getAttemptTimeOut() == null) {
                result.setAttemptTimeOut(this.getAttemptTimeOut());
            } else {
                result.setAttemptTimeOut(resultInfoForScript.getAttemptTimeOut());
            }

            if (resultInfoForScript.getFailTimeout() == null) {
                result.setFailTimeout(this.getFailTimeout());
            } else {
                result.setFailTimeout(resultInfoForScript.getFailTimeout());
            }
        }
        result.setProperties(this.getProperties());
        return result;
    }
}
