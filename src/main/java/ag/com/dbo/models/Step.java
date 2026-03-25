package ag.com.dbo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "step", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step {

    @Id
    @SequenceGenerator( name = "jpaSequence", sequenceName = "etl_step_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jpaSequence")
    @Column(name = "step_id", updatable = false)
//    @Column(name = "step_id", updatable = false, columnDefinition="numeric(38) DEFAULT nextval(\'etl_step_id_seq\')")

    private BigInteger stepId;
    private Boolean stepActive;
    private BigInteger[] parentSteps;


    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false, columnDefinition="") // Specifies the FK column name
    private Etl etl;

}
