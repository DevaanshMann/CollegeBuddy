package com.collegebuddy.groups;

import java.time.Instant;

public record GroupMessageDto(
        Long id,
        Long senderId,
        String senderName,
        String senderAvatar,
        String body,
        Instant sentAt
) {}
