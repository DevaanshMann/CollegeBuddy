package com.collegebuddy.auth;

import com.collegebuddy.dto.AuthResponse;
import com.collegebuddy.dto.ForgotPasswordRequest;
import com.collegebuddy.dto.LoginRequest;
import com.collegebuddy.dto.ResendVerificationRequest;
import com.collegebuddy.dto.ResetPasswordRequest;
import com.collegebuddy.dto.SignupRequest;
import com.collegebuddy.dto.UserDto;
import com.collegebuddy.dto.VerifyEmailRequest;
import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService,
                          TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        UserDto user = authService.getUserById(current.id());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        AuthResponse resp = authService.signup(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse resp = authService.login(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resend(@RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
