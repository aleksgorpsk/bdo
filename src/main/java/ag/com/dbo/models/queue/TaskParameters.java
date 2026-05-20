package ag.com.dbo.models.queue;

import lombok.Data;

import java.util.Map;

@Data
public class TaskParameters {
     private String commandProfile;
    private Map<String, String> parameters ;
    private String   calculateType;
}
