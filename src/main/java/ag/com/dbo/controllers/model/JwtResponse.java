package ag.com.dbo.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@AllArgsConstructor
@Data
public class JwtResponse {
    private String token;
}

