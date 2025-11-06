package com.collegebuddy.security;

import com.collegebuddy.common.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper to get the currently authenticated user.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser user) {
            return user;
        }

        throw new UnauthorizedException("Unexpected principal type");
    }
}
