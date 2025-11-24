package com.collegebuddy.testutil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtTestUtils {

    // Must match the secret in application-test.yml
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1pbnRlZ3JhdGlvbi10ZXN0cy1vbmx5LW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHM=";

    private static Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(TEST_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String makeValidJwt(Long userId, String campusDomain) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(3600);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("campusDomain", campusDomain))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String makeExpiredJwt(Long userId, String campusDomain) {
        Instant past = Instant.now().minusSeconds(3600);
        Instant exp = past.plusSeconds(1);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("campusDomain", campusDomain))
                .setIssuedAt(Date.from(past))
                .setExpiration(Date.from(exp))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String makeInvalidJwt() {
        return "invalid.jwt.token";
    }
}
