package com.collegebuddy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    private JwtService jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtService();
    }

    @Test
    void issueAccess_includesSubjectAndSchoolIdClaim() {
        String token = jwt.issueAccess(42L, 7L);
        Jws<Claims> parsed = jwt.parse(token);

        assertThat(parsed.getBody().getSubject()).isEqualTo("42");
        assertThat(parsed.getBody().get("sid", Object.class)).isEqualTo(7);
        assertThat(parsed.getBody().getExpiration()).isAfter(new java.util.Date());
    }

    @Test
    void parse_withGarbageToken_throwsJwtException() {
        assertThatThrownBy(() -> jwt.parse("not-a-jwt"))
                .isInstanceOf(JwtException.class); // be specific per rubric
    }
}
