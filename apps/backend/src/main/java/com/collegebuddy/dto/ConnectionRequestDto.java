package com.collegebuddy.dto;

public record ConnectionRequestDto(
        Long requestId,
        Long userId,
        String displayName,
        String avatarUrl,
        String visibility,
        String campusDomain
) {}
