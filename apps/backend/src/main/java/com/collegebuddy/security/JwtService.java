package com.collegebuddy.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    // Store a long, random, Base64-encoded secret in application.yml
    // example:
    // collegebuddy.jwt.secret: wK1L0... (base64)
    @Value("${collegebuddy.jwt.secret}")
    private String secretBase64;

    // Token lifetime (in seconds). You can externalize this too.
    @Value("${collegebuddy.jwt.ttlSeconds:3600}")
    private long ttlSeconds;

    private Key signingKey() {
        // Decode the Base64 secret and build an HMAC-SHA key, as recommended for HS256. :contentReference[oaicite:4]{index=4}
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String issueToken(Long userId, String campusDomain) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("campusDomain", campusDomain))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> parsed = parseClaims(token);
            // If parsing didn’t throw, signature+exp are valid
            return parsed.getBody().getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // expired, malformed, bad signature, etc.
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        // The modern JJWT API uses parserBuilder() + setSigningKey(...). :contentReference[oaicite:5]{index=5}
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token);
    }

    public Long extractUserId(String token) {
        Jws<Claims> parsed = parseClaims(token);
        return Long.parseLong(parsed.getBody().getSubject());
    }

    public String extractCampusDomain(String token) {
        Jws<Claims> parsed = parseClaims(token);
        Object claim = parsed.getBody().get("campusDomain");
        return claim != null ? claim.toString() : null;
    }
}
