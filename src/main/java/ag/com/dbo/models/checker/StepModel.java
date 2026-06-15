package ag.com.dbo.models.checker;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StepModel {

    private String logToTesultScript;

    private HashMap<String, Object> properties = new HashMap<>();
    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

    public Map<String, Object> getVars(){
        Map<String, Object> result = (Map<String, Object>) properties.clone();
        result.put("logToTesultScript", logToTesultScript);
        return  result;
    }
}
