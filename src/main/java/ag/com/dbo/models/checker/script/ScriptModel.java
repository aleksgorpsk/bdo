package ag.com.dbo.models.checker.script;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static ag.com.dbo.services.Utils.getScriptDefinitionFromFullName;

@Data
/**
 * full name = Language:name[:version]
 */
public class ScriptModel {
    private Long attemptTimeOut; // sec
    private Long failTimeout;
    private String resultName;


    /**
     *
     * @return name of result by result name or os constant
     */
    public String getScriptResultName(){
        if (StringUtils.isEmpty(this.resultName) ){
            return Constants.SCRIPT_RESULT;
        }
        return resultName;
    }

    /**
     *  language:scriptName:version
     * @param scriptName
     * @return
     */
    public ScriptRequest scriptRequest(String scriptName, StepInstance si){
        ScriptRequest result = new ScriptRequest();
        ScriptDefinition scriptId  = getScriptDefinitionFromFullName(scriptName);
        result.setScriptDefinition(scriptId);
        result.setStepName(si.getName());
        result.setVars(si.getVars());
        result.setLocalResults(si.getLocalResults());
        result.setEtlResults(si.getEtlInstance().getEtlVars());
        return  result;
    }

    private Map<String, Object> properties = new HashMap<>();
    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

}
