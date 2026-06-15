package ag.com.dbo.models.script;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
//  groovy:checkSensor:1
@Embeddable
public class ScriptId  implements Serializable {

    String language;
    String name;
    Integer version;

}
