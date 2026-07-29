package ag.com.dbo.models.script;

import jakarta.persistence.Embeddable;
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
    String type;
    String version;
    public ScriptId getScriptId(){
        return new ScriptId(this.language, this.name, this.type, this.version);
    }
}
