package ag.com.dbo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.OffsetDateTime;

@Entity
@Table(name = "step_instance", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepInstance {

    @Id
    @SequenceGenerator( name = "mySeqGen", sequenceName = "etl_step_instance_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mySeqGen")
//    @Column(name = "step_instance_id", updatable = false, columnDefinition="numeric(38) DEFAULT nextval(\'etl_step_instance_id_seq\')")
    @Column(name = "step_instance_id", updatable = false)

    private BigInteger stepInstanceId;

    private BigInteger[] parentStepInstanceIds;

    private Integer status; // 1- in progress, 3- finished successfully, 4- failed, 5 - in Queue

    private OffsetDateTime startDate;

    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "etl_instance_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private EtlInstance etlInstance;

    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "step_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private Step step;


    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private Etl etl;

}
