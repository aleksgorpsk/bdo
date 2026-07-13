package ag.com.dbo.models.checker.script;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
    public ScriptRequest scriptRequest(String scriptName){

        String[] ar =scriptName.split(":");
        ScriptRequest result = new ScriptRequest();
        result.setLanguage(ar[0]);
        result.setScriptName(ar[1]);
        if(ar.length>2){
            result.setVersion(ar[2]);
        }
        return  result;
    }

    private Map<String, Object> properties = new HashMap<>();
    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

}
