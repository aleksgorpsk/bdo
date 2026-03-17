package ag.com.dbo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.OffsetDateTime;

@Entity
@Table(name = "etl_instance", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtlInstance {
    @Id
    @SequenceGenerator( name = "jpaSequence", sequenceName = "etl_id_etl_instance_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jpaSequence")
    @Column(name = "id_etl_instance", updatable = false, nullable = false)
    private BigInteger idEtlInstance;

    private String status;
    private OffsetDateTime startDate;
    private Integer attempt;

    private String comment;
    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY) // Many steps to one etl
    @JoinColumn(name = "etl_id", nullable = false) // Specifies the FK column name
    private Etl etl;


}
