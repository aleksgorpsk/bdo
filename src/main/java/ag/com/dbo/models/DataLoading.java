package ag.com.dbo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "data_loading", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataLoading {
    @Id
    @SequenceGenerator( name = "dataLoadingSq", sequenceName = "etl_data_loading_id_seq", allocationSize = 1, initialValue=1000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataLoadingSq")
    @Column(name = "data_loading_id", updatable = false)
    private BigInteger id;
    private String name;
    private Boolean active;
    @Column(columnDefinition = "TEXT")
    private  String props;

}
