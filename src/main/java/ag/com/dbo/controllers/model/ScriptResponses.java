package ag.com.dbo.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScriptResponses {
    List<ScriptResponse> responses;

    public void addResponse(ScriptResponse response){
    if (responses == null){
        responses = new ArrayList<ScriptResponse>();
    }
    responses.add(response);
    }
}
