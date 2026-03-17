package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.JwtResponse;
import ag.com.dbo.controllers.model.LoginRequest;
import ag.com.dbo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private  JwtUtil jwtUtil;

    public AuthController() {

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Demo validation (replace with DB check in real apps)
        if (!"password".equals(request.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtUtil.generateToken(request.getUsername());

       return ResponseEntity.status(HttpStatus.OK).header("Content-Type","application/json").body(new JwtResponse(token));
    }

}
