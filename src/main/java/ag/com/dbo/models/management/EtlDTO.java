package ag.com.dbo.models.management;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigInteger;

@Data
public class EtlDTO {
    @Id
    private BigInteger id;
    @NotBlank
    private String name;
    private Boolean active;
    private String status;
    private String cronScheduling;
    private Integer secondInterval; // in sec
    private String comment;
}
