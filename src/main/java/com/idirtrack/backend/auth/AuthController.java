package com.idirtrack.backend.auth;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<BasicResponse> login(@RequestBody AuthRequest request) {
        // Try to authenticate the user
        try {
            BasicResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } 
        // Catch the BasicException and return a bad request
        catch (BasicException e) {
            return ResponseEntity.badRequest().body(e.getResponse());
        }
        // Catch the exception and return a bad request
        catch (Exception e) {
            return ResponseEntity.badRequest().body(BasicResponse.builder().message(e.getMessage()).build());
        }
    }
    
}
