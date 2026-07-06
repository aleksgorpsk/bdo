package ag.com.dbo.models.management;

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
    @SequenceGenerator( name = "mySeqGen", sequenceName = "etl_id_seq", allocationSize = 1, initialValue=1000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mySeqGen")
    @Column(name = "id", updatable = false)
    private BigInteger id;
    private String name;
    private String comment;
    private Boolean active;
    private String status;
    private String cronScheduling;
    private Integer secondInterval;
}
