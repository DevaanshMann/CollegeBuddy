package com.collegebuddy.dto;

/**
 * Returned after signup/login.
 * status could be "pending", "ok", etc.
 * jwt may be null if not yet verified.
 */
public record AuthResponse(
        String status,
        String jwt
) {}
