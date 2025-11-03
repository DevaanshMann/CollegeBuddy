package com.collegebuddy.testutil;

/**
 * Helpers to generate valid/invalid JWTs during tests
 * without going through full login flow.
 */
public class JwtTestUtils {

    public static String makeValidJwt(Long userId, String campusDomain) {
        return "stub.jwt.for.user." + userId;
    }
}
