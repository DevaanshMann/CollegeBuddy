package com.collegebuddy.auth;

import org.springframework.stereotype.Service;

/**
 * Issues and validates verification tokens for email activation.
 */
@Service
public class TokenService {

    public String generateVerificationToken(Long userId) {
        // TODO: generate and persist a token
        return "stub-token";
    }

    public boolean validateVerificationToken(String token) {
        // TODO: lookup token in DB, check expiry
        return true;
    }

    public void markUserActive(String token) {
        // TODO: activate user after valid token
    }
}
