package ag.com.dbo.models.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "node", schema = "etl" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    @Id
    @SequenceGenerator( name = "mySeqGen", sequenceName = "node_id_seq", allocationSize = 1, initialValue=100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mySeqGen")
    @Column(name = "id", updatable = false)
    private Integer id;
    private String name;
    private String host;
    private String type;  // master/agent

    @Column(columnDefinition ="TEXT")
    private String tags; // coma separated  string
    private Boolean active;
    @Transient
    private Integer freeSlots;
    @Transient
    private Integer busy;

}
