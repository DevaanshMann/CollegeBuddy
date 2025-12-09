package com.collegebuddy.dto;

public record UserDto(
        Long id,
        String email,
        String displayName,
        String campusDomain,
        String avatarUrl,
        String profileVisibility,
        String role
) {}
