package ag.com.dbo.models.script;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "script", schema = "script" )
@Data
@NoArgsConstructor
@AllArgsConstructor
//  groovy:checkSensor:1
public class Script implements Serializable {

    @EmbeddedId
    private ScriptId scriptId;
    @Column(columnDefinition = "Text")
    private String script;

}
