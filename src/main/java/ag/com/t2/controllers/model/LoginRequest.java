package ag.com.t2.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public  class LoginRequest {
    private String username;
    private String password;
}