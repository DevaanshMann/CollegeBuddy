package com.collegebuddy.dto;

import java.time.Instant;

public record BlockedUserDto(
    Long id,
    Long userId,          // The blocked user's ID
    String displayName,   // The blocked user's display name
    String avatarUrl,     // The blocked user's avatar
    Instant blockedAt
) {}
