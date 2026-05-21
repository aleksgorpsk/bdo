package ag.com.dbo.services.queue.model;

import ag.com.dbo.models.queue.QueueStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropData {

    private int ResultStatus = -100;
    private QueueStorage qs;
    private String message;
}
