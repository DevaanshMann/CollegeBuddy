package com.collegebuddy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtService {
    private final String issuer = "collegebuddy";
    // For dev only; in prod read from env and ensure 256-bit key
    private final Key key = Keys.hmacShaKeyFor("change-me-in-dev-change-me-in-dev".getBytes());

    public String issueAccess(Long userId, Long schoolId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .addClaims(Map.of("sid", schoolId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(900)))
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
