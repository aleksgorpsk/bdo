package ag.com.dbo.models.script;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ScriptDefinition implements Serializable {
    String language;
    String name;
    String version;

    public String getFullName() {
        return String.format("%s:%s:%s", language, name, version);
    }
}
