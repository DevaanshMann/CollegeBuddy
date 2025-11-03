package com.collegebuddy.dto;

/**
 * Lightweight view of a user for search / profile list.
 */
public record UserDto(
        Long userId,
        String displayName,
        String avatarUrl,
        String visibility,
        String campusDomain
) {}
