package com.collegebuddy.admin;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.Role;

import java.time.Instant;

public record AdminUserDto(
        Long userId,
        String email,
        String displayName,
        String campusDomain,
        AccountStatus status,
        Role role,
        String avatarUrl,
        Instant createdAt
) {
}
