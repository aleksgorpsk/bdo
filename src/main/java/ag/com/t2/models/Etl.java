package ag.com.t2.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "etl", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Etl {
    @Id
    @SequenceGenerator( name = "jpaSequence", sequenceName = "etl_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jpaSequence")
    @Column(name = "id", updatable = false, nullable = false)
    private BigInteger id;
    private String name;
    private Boolean active;
    private String cronScheduling;
    private Integer interval; // in sec
    private String comment;

}
