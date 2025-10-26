package com.collegebuddy.dto.user;

import java.time.Instant;

public record UserDetailDto(
        Long id,
        String displayName,
        String email,
        String bio,
        String avatarUrl,
        String school,
        Instant createdAt
) {}