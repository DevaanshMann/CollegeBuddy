package com.collegebuddy.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set a test secret (Base64 encoded 256-bit key for HS256)
        ReflectionTestUtils.setField(jwtService, "secretBase64", "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2VzMTIzNDU2Nzg5MA==");
        ReflectionTestUtils.setField(jwtService, "ttlSeconds", 3600L);
    }

    @Test
    void issueToken_shouldGenerateValidJwt() {
        String token = jwtService.issueToken(1L, "cpp.edu", "STUDENT", "test@cpp.edu", "Test User");

        assertThat(token).isNotNull();
        assertThat(token).contains(".");
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtService.issueToken(1L, "cpp.edu", "STUDENT", "test@cpp.edu", "Test User");

        boolean isValid = jwtService.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Create service with 0 TTL to immediately expire tokens
        JwtService expiredTokenService = new JwtService();
        ReflectionTestUtils.setField(expiredTokenService, "secretBase64", "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2VzMTIzNDU2Nzg5MA==");
        ReflectionTestUtils.setField(expiredTokenService, "ttlSeconds", 0L);

        String token = expiredTokenService.issueToken(1L, "cpp.edu", "STUDENT", "test@cpp.edu", "Test User");

        // Sleep briefly to ensure expiration
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = expiredTokenService.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = jwtService.issueToken(1L, "cpp.edu", "STUDENT", "test@cpp.edu", "Test User");

        // Tamper with the token by changing a character
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        boolean isValid = jwtService.validateToken(tamperedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        Long userId = 123L;
        String token = jwtService.issueToken(userId, "cpp.edu", "STUDENT", "test@cpp.edu", "Test User");

        Long extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void extractUserId_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtService.extractUserId(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractCampusDomain_shouldReturnCorrectCampusDomain() {
        String campusDomain = "cpp.edu";
        String token = jwtService.issueToken(1L, campusDomain, "STUDENT", "test@cpp.edu", "Test User");

        String extractedDomain = jwtService.extractCampusDomain(token);

        assertThat(extractedDomain).isEqualTo(campusDomain);
    }

    @Test
    void extractCampusDomain_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtService.extractCampusDomain(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void issueToken_shouldIncludeAllClaims() {
        String token = jwtService.issueToken(1L, "cpp.edu", "STUDENT", "test@cpp.edu", "Test User");

        Long userId = jwtService.extractUserId(token);
        String campusDomain = jwtService.extractCampusDomain(token);

        assertThat(userId).isEqualTo(1L);
        assertThat(campusDomain).isEqualTo("cpp.edu");
    }

    @Test
    void issueToken_shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtService.issueToken(1L, "cpp.edu", "STUDENT", "user1@cpp.edu", "User One");
        String token2 = jwtService.issueToken(2L, "cpp.edu", "STUDENT", "user2@cpp.edu", "User Two");

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtService.extractUserId(token1)).isEqualTo(1L);
        assertThat(jwtService.extractUserId(token2)).isEqualTo(2L);
    }
}
