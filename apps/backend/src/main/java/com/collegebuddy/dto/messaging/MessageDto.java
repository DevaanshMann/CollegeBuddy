package com.collegebuddy.dto.messaging;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long threadId,
        Long senderId,
        String text,
        Instant sentAt
) {}