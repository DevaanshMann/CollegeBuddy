package com.collegebuddy.auth;

import com.collegebuddy.dto.AuthResponse;
import com.collegebuddy.dto.LoginRequest;
import com.collegebuddy.dto.SignupRequest;
import org.springframework.stereotype.Service;

/**
 * Business logic for registration, login, etc.
 */
@Service
public class AuthService {

    public AuthResponse signup(SignupRequest request) {
        // TODO: create user with PENDING_VERIFICATION and send email
        return new AuthResponse("pending", null);
    }

    public AuthResponse login(LoginRequest request) {
        // TODO: validate password, return JWT
        return new AuthResponse("ok", "stub.jwt.token");
    }
}
