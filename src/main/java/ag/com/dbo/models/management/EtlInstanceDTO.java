package ag.com.dbo.models.management;


import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigInteger;
import java.time.OffsetDateTime;

@Data
public class EtlInstanceDTO {
    @Id
    private BigInteger etlInstanceId;
    private String status;
    private OffsetDateTime start;
    private OffsetDateTime stop;
    private String comment;
    private String name;
    private Etl etl;
    private Boolean active;

}
