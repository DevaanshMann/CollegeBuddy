package com.collegebuddy.dto.messaging;

import java.time.Instant;

public record ThreadDto(
        Long id,
        Long userAId,
        Long userBId,
        Instant lastActivityAt,
        Integer unreadCount
) {}