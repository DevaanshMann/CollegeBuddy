package com.collegebuddy.groups;

import com.collegebuddy.domain.Visibility;
import java.time.Instant;

public record GroupDto(
        Long id,
        String name,
        String description,
        String campusDomain,
        Long creatorId,
        String creatorName,
        Visibility visibility,
        long memberCount,
        boolean isMember,
        boolean isAdmin,
        long unreadCount,
        Instant createdAt
) {
}
