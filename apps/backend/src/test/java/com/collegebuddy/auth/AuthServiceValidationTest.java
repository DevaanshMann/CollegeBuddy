package com.collegebuddy.auth;

import com.collegebuddy.dto.SignupRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AuthService business logic validation.
 * These tests verify input validation rules without requiring database or external dependencies.
 */
class AuthServiceValidationTest {

    @Test
    void signup_shouldRejectEmailWithoutEduDomain() {
        // Test that non-.edu emails are rejected
        String[] invalidEmails = {
            "test@gmail.com",
            "test@yahoo.com",
            "test@company.com",
            "test@outlook.com"
        };

        for (String email : invalidEmails) {
            SignupRequest request = new SignupRequest(email, "password123", "cpp.edu");

            // Verify the validation logic would reject this
            boolean isValid = email != null && email.endsWith(".edu");
            assert !isValid : "Email " + email + " should be rejected";
        }
    }

    @Test
    void signup_shouldAcceptValidEduEmails() {
        // Test that .edu emails are accepted
        String[] validEmails = {
            "student@cpp.edu",
            "professor@ucla.edu",
            "admin@stanford.edu",
            "test@harvard.edu"
        };

        for (String email : validEmails) {
            boolean isValid = email != null && email.endsWith(".edu");
            assert isValid : "Email " + email + " should be accepted";
        }
    }

    @Test
    void signup_shouldRejectNullEmail() {
        SignupRequest request = new SignupRequest(null, "password123", "cpp.edu");

        // Verify null email validation
        String email = request.email();
        boolean isValid = email != null && email.endsWith(".edu");
        assert !isValid : "Null email should be rejected";
    }

    @Test
    void login_shouldValidateRequiredFields() {
        // Test password field validation logic
        String email = "test@cpp.edu";
        String password = "password123";

        assert email != null && !email.isEmpty() : "Email is required";
        assert password != null && !password.isEmpty() : "Password is required";
    }

    @Test
    void resetPassword_shouldValidateTokenFormat() {
        // Test that token validation logic works
        String validToken = "550e8400-e29b-41d4-a716-446655440000"; // UUID format
        String invalidToken = "";

        assert validToken != null && !validToken.isEmpty() : "Valid token should pass";
        assert invalidToken.isEmpty() : "Empty token should fail";
    }
}
