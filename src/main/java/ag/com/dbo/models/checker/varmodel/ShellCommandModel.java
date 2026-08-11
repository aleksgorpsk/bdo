package ag.com.dbo.models.checker.varmodel;

import lombok.Data;

@Data
public class ShellCommandModel   extends CommonModel{

    private Long failTimeout;
    private String resultName;
}
