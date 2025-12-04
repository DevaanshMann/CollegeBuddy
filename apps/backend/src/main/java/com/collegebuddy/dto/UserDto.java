package com.collegebuddy.dto;

public record UserDto(
        Long userId,
        String displayName,
        String avatarUrl,
        String visibility,
        String campusDomain
) {}
