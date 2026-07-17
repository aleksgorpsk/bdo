package ag.com.dbo.controllers.model;

import ag.com.dbo.models.script.ScriptDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScriptResponse {
    private ScriptDefinition scriptDefinition;
    private String status;
    private String response;
}
