package ag.com.dbo.models;

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
    private Integer status; //1 - ready to start 2- started
    private String cronScheduling;
    private Integer interval; // in sec
    private String comment;
}
