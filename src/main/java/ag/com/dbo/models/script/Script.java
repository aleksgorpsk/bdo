package ag.com.dbo.models.script;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "script", schema = "script")
@Data
@NoArgsConstructor
@AllArgsConstructor
//  groovy:checkSensor.common:1
public class Script{

    @Id
    @SequenceGenerator(name = "mySeqGen", sequenceName = "script_id_seq", allocationSize = 1, initialValue = 1000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mySeqGen")
    @Column(name = "id", updatable = false)
    private BigInteger id;

    private String language;

    private String name;

    @Column(name = "type", updatable = false)
    private String type;

    private String version;

    @Column(name = "script", columnDefinition = "Text")
    private String script;

    private Boolean active;


}
