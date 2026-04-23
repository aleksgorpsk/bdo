package ag.com.dbo.models.graf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rectangle extends Figure {
    private int x;
    private int y;
    private int w;
    private int h;
    private int rx=20;
    private int ry=20;

    private String color;
    private final String  type="rectangle" ;

}
