package com.collegebuddy.dto;

/**
 * DTO for connection requests that includes both the request ID and user information.
 */
public record ConnectionRequestDto(
        Long requestId,
        Long userId,
        String displayName,
        String avatarUrl,
        String visibility,
        String campusDomain
) {}
