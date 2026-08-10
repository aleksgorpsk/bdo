package ag.com.dbo.services.queue.model;

import ag.com.dbo.models.queue.QueueStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ag.com.dbo.services.queue.utils.QueueConstants.UNKNOWN_ERROR;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropData {

    private int ResultStatus = UNKNOWN_ERROR;
    private QueueStorage qs;
    private String message;
}
