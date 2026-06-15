package ag.com.dbo.models.checker;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SensorModel {
    private Long attemptTimeOut;
    private Long failTimeout;
    private String checkerScript;
    private String sensorResultName;

    private Map<String, Object> properties = new HashMap<>();
    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

}
