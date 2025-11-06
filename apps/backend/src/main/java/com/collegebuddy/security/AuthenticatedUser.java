package com.collegebuddy.security;

/**
 * Represents the authenticated user from JWT.
 */
public record AuthenticatedUser(
        Long id,
        String campusDomain
) {}
