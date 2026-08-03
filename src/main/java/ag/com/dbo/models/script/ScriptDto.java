package ag.com.dbo.models.script;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
//  groovy:checkSensor.common:1
public class ScriptDto implements Serializable {

    private BigInteger id;
    private String language;
    private String name;
    private String type;
    private String version;
    private String script;
    private Boolean active;


}
