package ag.com.dbo.models;

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
//    @Column(name = "id", updatable = false, columnDefinition="numeric(38) DEFAULT nextval(\'etl_id_seq\')")

    private BigInteger id;
    private String name;
    private Boolean active;
    private Integer status; //1 - ready to start, 2- auto started, 3 - manual start
    private String cronScheduling;
    private Integer interval; // in sec
    private String comment;
    private Integer lastResult;

}
