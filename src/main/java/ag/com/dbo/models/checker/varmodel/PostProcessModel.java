package ag.com.dbo.models.checker.varmodel;

import lombok.Data;

@Data
public class PostProcessModel  extends CommonModel{
    private Long timeout;
    private String resultName;
}
