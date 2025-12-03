package com.collegebuddy.groups;

import com.collegebuddy.domain.GroupRole;
import java.time.Instant;

public record GroupMemberDto(
        Long userId,
        String displayName,
        String avatarUrl,
        GroupRole role,
        Instant joinedAt
) {
}
