package ag.com.dbo.models.graf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Line extends Figure {
    private int x1;
    private int y1;
    private int  x2;
    private int  y2;
    private String  stroke;
    @JsonProperty("strokeWidth")
    private int  strokeWidth;
    @Setter(AccessLevel.NONE)
    private final String  type="line";
}
