package ag.com.dbo.models.checker.varmodel;

import lombok.Data;

@Data
public class SensorModel   extends CommonModel{

    private Long attemptTimeOut;
    private Long failTimeout;
}
