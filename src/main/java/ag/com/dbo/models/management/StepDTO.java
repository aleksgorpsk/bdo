package ag.com.dbo.models.management;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StepDTO {
    @Id
    private BigInteger stepId;
    @NotBlank
    private String name;

    private Boolean stepActive;
    private BigInteger[] parentStepIds;
    private BigInteger etlId;
    private Etl etl;

    private DataLoading dataLoading;

    private String vars;
    private Integer maxAttempts;
    private Boolean saveCalculate;

    private String script;

    String stepType;
    String[] stepTypeList;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime nextTest;
}
