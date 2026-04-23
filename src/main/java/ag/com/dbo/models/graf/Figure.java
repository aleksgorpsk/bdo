package ag.com.dbo.models.graf;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.math.BigInteger;

@Data
public class Figure {
    private BigInteger id;
    @Setter(AccessLevel.NONE)
    private  String  type;

}
