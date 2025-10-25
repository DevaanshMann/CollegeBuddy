package com.collegebuddy.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthFilterNoSidAndWrongKeyTest {

    @Test
    void tokenWithoutSid_setsPrincipalButNoSchoolAttr() throws Exception {
        String secret = "test-secret-test-secret-test-secret-test-123456";
        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        // token with subject but NO "sid" claim
        String token = Jwts.builder()
                .setIssuer("collegebuddy")
                .setSubject("42")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(900)))
                .signWith(key)
                .compact();

        JwtService verifier = new JwtService(secret);
        JwtAuthFilter filter = new JwtAuthFilter(verifier);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("42");
        assertThat(req.getAttribute("schoolId")).isNull();
    }

    @Test
    void tokenSignedWithWrongKey_isIgnored() throws Exception {
        String a = "key-A-key-A-key-A-key-A-key-A-key-A-123456";
        String b = "key-B-key-B-key-B-key-B-key-B-key-B-123456";

        JwtService signer = new JwtService(a);
        String token = signer.issueAccess(10L, 3L);

        JwtService verifier = new JwtService(b);
        JwtAuthFilter filter = new JwtAuthFilter(verifier);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
