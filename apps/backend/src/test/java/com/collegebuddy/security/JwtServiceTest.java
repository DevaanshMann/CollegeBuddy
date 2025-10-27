//package com.collegebuddy.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.*;
//
//class JwtServiceTest {
//
//    private JwtService jwt;
//
//    @BeforeEach
//    void setUp() {
//        jwt = new JwtService();
//    }
//
//    @Test
//    void issueAccess_includesSubjectAndSchoolIdClaim() {
//        String token = jwt.issueAccess(42L, 7L);
//        Jws<Claims> parsed = jwt.parse(token);
//
//        assertThat(parsed.getBody().getSubject()).isEqualTo("42");
//        assertThat(parsed.getBody().get("sid", Object.class)).isEqualTo(7);
//        assertThat(parsed.getBody().getExpiration()).isAfter(new java.util.Date());
//    }
//
//    @Test
//    void parse_withGarbageToken_throws() {
//        assertThatThrownBy(() -> jwt.parse("not-a-jwt"))
//                .isInstanceOf(Exception.class); // signature/format error
//    }
//}

package com.collegebuddy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    // >= 32 bytes (HS256 requirement)
    private static final String TEST_SECRET = "0123456789_0123456789_0123456789_012345";

    // ----------------------------------------------------------------------
    // issueAccess_and_parse_roundTrip_claimsAreCorrect
    // ----------------------------------------------------------------------

    @Test
    void issueAccess_and_parse_roundTrip_claimsAreCorrect() {
        JwtService jwt = new JwtService(TEST_SECRET);

        long userId = 42L;
        long schoolId = 7L;

        Instant before = Instant.now();
        String token = jwt.issueAccess(userId, schoolId);
        Instant after = Instant.now();

        Jws<Claims> jws = jwt.parse(token);
        Claims c = jws.getBody();

        // issuer, subject, sid
        assertThat(c.getIssuer()).isEqualTo("collegebuddy");
        assertThat(c.getSubject()).isEqualTo(Long.toString(userId));

        Number sid = c.get("sid", Number.class);
        assertThat(sid.longValue()).isEqualTo(schoolId);

        // iat between before/after; exp ~ 15 minutes after iat
        Date iat = c.getIssuedAt();
        Date exp = c.getExpiration();

        assertThat(iat.toInstant()).isBetween(before.minusSeconds(1), after.plusSeconds(1));

        long diffSeconds = ChronoUnit.SECONDS.between(iat.toInstant(), exp.toInstant());
        // allow small timing jitter
        assertThat(diffSeconds).isBetween(14 * 60L, 16 * 60L);
    }

    // +1: Large ID values still round-trip correctly
    @Test
    void issueAccess_roundTrip_withLargeIds() {
        JwtService jwt = new JwtService(TEST_SECRET);

        long userId = Long.MAX_VALUE - 123;
        long schoolId = Long.MAX_VALUE - 456;

        String token = jwt.issueAccess(userId, schoolId);
        Claims c = jwt.parse(token).getBody();

        assertThat(c.getSubject()).isEqualTo(Long.toString(userId));
        Number sid = c.get("sid", Number.class);
        assertThat(sid.longValue()).isEqualTo(schoolId);
    }

    // +2: Expiration window sanity (looser bounds to avoid flakiness)
    @Test
    void issueAccess_expirationWindow_isAbout15Minutes() {
        JwtService jwt = new JwtService(TEST_SECRET);
        String token = jwt.issueAccess(1L, 2L);
        Claims c = jwt.parse(token).getBody();

        long diffSeconds = ChronoUnit.SECONDS.between(
                c.getIssuedAt().toInstant(),
                c.getExpiration().toInstant()
        );
        assertThat(diffSeconds).isBetween(14 * 60L, 16 * 60L);
    }

    // ----------------------------------------------------------------------
    // parse_withDifferentKey_failsVerification
    // ----------------------------------------------------------------------

    @Test
    void parse_withDifferentKey_failsVerification() {
        JwtService jwtA = new JwtService(TEST_SECRET);
        JwtService jwtB = new JwtService("ANOTHER_SECRET_STRING_AT_LEAST_32_BYTES!!");

        String tokenFromA = jwtA.issueAccess(1L, 2L);

        assertThrows(JwtException.class, () -> jwtB.parse(tokenFromA));
    }

    // +1: Same key should succeed (sanity check)
    @Test
    void parse_withSameKey_succeeds() {
        JwtService jwt = new JwtService(TEST_SECRET);
        String token = jwt.issueAccess(11L, 22L);
        Claims c = jwt.parse(token).getBody();

        assertThat(c.getSubject()).isEqualTo("11");
        Number sid = c.get("sid", Number.class);
        assertThat(sid.longValue()).isEqualTo(22L);
    }

    // +2: Key with same length but different bytes must fail verification
    @Test
    void parse_withRotatedKey_failsVerification() {
        // Same length as TEST_SECRET but shifted one char
        String rotated = TEST_SECRET.substring(1) + TEST_SECRET.charAt(0);
        JwtService jwtA = new JwtService(TEST_SECRET);
        JwtService jwtB = new JwtService(rotated);

        String tokenFromA = jwtA.issueAccess(3L, 4L);
        assertThrows(JwtException.class, () -> jwtB.parse(tokenFromA));
    }

    // ----------------------------------------------------------------------
    // parse_withTamperedToken_throws
    // ----------------------------------------------------------------------

    @Test
    void parse_withTamperedToken_throws() {
        JwtService jwt = new JwtService(TEST_SECRET);
        String token = jwt.issueAccess(123L, 456L);

        // Tamper with the token (break signature)
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> jwt.parse(tampered));
    }

    // +1: Tamper the header (change first character) -> should throw
    @Test
    void parse_withHeaderTampered_throws() {
        JwtService jwt = new JwtService(TEST_SECRET);
        String token = jwt.issueAccess(10L, 20L);

        // Flip first character of the token
        char first = token.charAt(0);
        char flipped = (first == 'a') ? 'b' : 'a';
        String tampered = flipped + token.substring(1);

        assertThrows(JwtException.class, () -> jwt.parse(tampered));
    }

    // +2: Tamper payload segment (still base64-decodable but signature invalid) -> throw
    @Test
    void parse_withPayloadTampered_throws() {
        JwtService jwt = new JwtService(TEST_SECRET);
        String token = jwt.issueAccess(99L, 88L);

        // JWT is header.payload.signature; split on '.'
        String[] parts = token.split("\\.");
        // mutate one char in the payload (middle) safely
        String payload = parts[1];
        char ch = payload.charAt(0);
        char alt = (ch == 'A') ? 'B' : 'A';
        parts[1] = alt + payload.substring(1);

        String tampered = String.join(".", parts);
        assertThrows(JwtException.class, () -> jwt.parse(tampered));
    }

    // ----------------------------------------------------------------------
    // constructor_rejectsTooShortSecret
    // ----------------------------------------------------------------------

    @Test
    void constructor_rejectsTooShortSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short-secret"));
    }

    // +1: Exactly 31 bytes should fail; exactly 32 should succeed
    @Test
    void constructor_edgeLengths() {
        String thirtyOne = "1234567890123456789012345678901"; // 31
        String thirtyTwo = "12345678901234567890123456789012"; // 32

        assertThrows(IllegalArgumentException.class, () -> new JwtService(thirtyOne));

        JwtService ok = new JwtService(thirtyTwo);
        String token = ok.issueAccess(1L, 1L);
        // parses fine
        assertThat(ok.parse(token)).isNotNull();
    }

    // +2: Very long secret should still work
    @Test
    void constructor_veryLongSecret_works() {
        String longSecret = "x".repeat(256);
        JwtService jwt = new JwtService(longSecret);
        String token = jwt.issueAccess(5L, 6L);
        assertThat(jwt.parse(token)).isNotNull();
    }

    // ----------------------------------------------------------------------
    // noArgConstructor_producesUsableService
    // ----------------------------------------------------------------------

    @Test
    void noArgConstructor_producesUsableService() {
        JwtService jwt = new JwtService(); // default secret meets length requirement
        String token = jwt.issueAccess(9L, 8L);

        Jws<Claims> parsed = jwt.parse(token);
        assertThat(parsed.getBody().getSubject()).isEqualTo("9");

        Number sid = parsed.getBody().get("sid", Number.class);
        assertThat(sid.longValue()).isEqualTo(8L);
    }

    // +1: Multiple tokens from no-arg ctor have distinct subjects as issued
    @Test
    void noArgConstructor_multipleTokens_distinctSubjects() {
        JwtService jwt = new JwtService();

        String t1 = jwt.issueAccess(101L, 1L);
        String t2 = jwt.issueAccess(202L, 1L);

        assertThat(jwt.parse(t1).getBody().getSubject()).isEqualTo("101");
        assertThat(jwt.parse(t2).getBody().getSubject()).isEqualTo("202");
    }

    // +2: Issuer and sid type consistency with default secret
    @Test
    void noArgConstructor_issuerAndSid_consistency() {
        JwtService jwt = new JwtService();
        String token = jwt.issueAccess(333L, 444L);
        Claims c = jwt.parse(token).getBody();

        assertThat(c.getIssuer()).isEqualTo("collegebuddy");
        Number sid = c.get("sid", Number.class);
        assertThat(sid.longValue()).isEqualTo(444L);
    }
}
