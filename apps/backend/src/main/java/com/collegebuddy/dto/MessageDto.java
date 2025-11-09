package com.collegebuddy.dto;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long senderId,
        String body,
        Instant sentAt
) {}
