package ag.com.dbo.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import static ag.com.dbo.utils.Utils.objectToMap;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScriptResponses {
    List<Map<String,Object>> responses;

    public void addResponse(Object o){
    if (responses == null){
        responses = new ArrayList<Map<String,Object>>();
    }
    responses.add(objectToMap(o));
    }
}
