package ag.com.dbo.services.loadingService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropData {

    private int ResultStatus = -100;
    private Object obj;
}
