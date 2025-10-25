package com.collegebuddy.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
class JwtAuthFilterExpiredTest {

    @Test
    void expiredToken_leavesContextEmpty() throws Exception {
        String secret = "test-secret-test-secret-test-secret-test-123456";
        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        // build an already-expired token
        String token = Jwts.builder()
                .setIssuer("collegebuddy")
                .setSubject("42")
                .setIssuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .setExpiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(key)
                .compact();

        JwtService verifier = new JwtService(secret);
        JwtAuthFilter filter = new JwtAuthFilter(verifier);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
