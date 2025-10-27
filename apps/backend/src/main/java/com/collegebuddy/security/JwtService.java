package com.collegebuddy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final String ISSUER = "collegebuddy";
    private static final int MIN_SECRET_BYTES = 32; // HS256 requires >= 256-bit key

    private final Key key;


    public JwtService(@Value("${JWT_SECRET:change-me-in-dev-change-me-in-dev}") String secret) {
        this.key = buildKey(secret);
    }

    public JwtService() {
        this("change-me-in-dev-change-me-in-dev");
    }

    private static Key buildKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least " + MIN_SECRET_BYTES + " bytes for HS256");
        }
        return Keys.hmacShaKeyFor(bytes);

    }

    public String issueAccess(Long userId, Long schoolId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(ISSUER)
                .setSubject(userId.toString())
                .addClaims(Map.of("sid", schoolId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(900))) // 15 minutes
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}