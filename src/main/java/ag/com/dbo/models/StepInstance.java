package ag.com.dbo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.OffsetDateTime;

@Entity
@Table(name = "step", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepInstance {

    @Id
    @SequenceGenerator( name = "jpaSequence", sequenceName = "etl_step_instance_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jpaSequence")
    @Column(name = "step_instance_id", updatable = false, nullable = false)
    private BigInteger stepInstanceId;

    private Integer  stepOrder;
    private Integer status;
    private OffsetDateTime startDate;


    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "step_id", nullable = false) // Specifies the FK column name
    private Step step;


    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false) // Specifies the FK column name
    private Etl etl;

}
