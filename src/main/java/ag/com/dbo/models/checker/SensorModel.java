package ag.com.dbo.models.checker;

import ag.com.dbo.controllers.model.ScriptRequest;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SensorModel {
    private Long attemptTimeOut; // sec
    private Long failTimeout;
    private String sensorResultName;
    private String sensorScript;


    public ScriptRequest scriptRequest(){
        if (sensorScript==null){
            return null;
        }
        String[] ar =sensorScript.split(":");
        ScriptRequest result = new ScriptRequest();
        result.setLanguage(ar[0]);
        result.setScriptName(ar[1]);
        if(ar.length>2){
            result.setVersion(Integer.parseInt(ar[2]));
        }
        return  result;
    }

    private Map<String, Object> properties = new HashMap<>();
    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

}
