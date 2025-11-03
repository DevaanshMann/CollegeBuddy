package com.collegebuddy.auth;

import com.collegebuddy.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Handles signup, login, email verification, resend verification
 */

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request){
//        TODO: creating pending user, send verification email
        return ResponseEntity.ok(new AuthResponse("Pending", null));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
//        TODO: Validate credentials, issue JWT
        return ResponseEntity.ok(new AuthResponse("Ok", "stub.jwt.token"));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerifyEmailRequest request){
//        TODO: validate token and activate account
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendVerification(@RequestBody ResendVerificationRequest request){
//        TODO: resend verification link
        return ResponseEntity.ok().build();
    }
}
