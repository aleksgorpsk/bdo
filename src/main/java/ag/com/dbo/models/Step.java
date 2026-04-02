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
    @SequenceGenerator( name = "jpaSequence", sequenceName = "etl_step_id_seq", allocationSize = 1, initialValue=1000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jpaSequence")
    @Column(name = "step_id", updatable = false)

    private BigInteger stepId;
    private Boolean stepActive;
    private BigInteger[] parentStepIds;
    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false) // Specifies the FK column name
    private Etl etl;

    @ManyToOne(fetch = FetchType.EAGER) // Many steps to one etl
    @JoinColumn(name = "data_loading_id") // Specifies the FK column name
    private DataLoading dataLoading;

    @Column(columnDefinition = "TEXT")
    private String vars;
    private String calculateMethod; // method of loading.... from ag.com.dbo.services.loadingService.TaskName
}
