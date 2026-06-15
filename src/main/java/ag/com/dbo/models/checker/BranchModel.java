package ag.com.dbo.models.checker;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BranchModel {
    private String branchScript;
    private String resultName;

    private Map<String, Object> properties = new HashMap<>();
    @JsonAnySetter
    public void setProperties(String key, Object value) {
        this.properties.put(key, value);
    }

}
