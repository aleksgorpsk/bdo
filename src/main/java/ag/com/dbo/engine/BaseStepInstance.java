package ag.com.dbo.engine;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BaseStepInstance {
    private BigInteger idStepInstance;
    private Integer Status;
    private String Name;
    private String VarInput;
    private String VarOutput;

}
