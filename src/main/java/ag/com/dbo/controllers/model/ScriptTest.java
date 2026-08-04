package ag.com.dbo.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
//  groovy:checkSensor.common:1
public class ScriptTest implements Serializable {

    private BigInteger id;
    private String language;
    private String name;
    private String type;
    private String version;
    private String script;
    private String stepName;

    private String vars;
    private String etlResults;
    private String localResults;

    private String error;
    private String response;
}
