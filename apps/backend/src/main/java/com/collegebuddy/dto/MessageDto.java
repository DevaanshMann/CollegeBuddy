package com.collegebuddy.dto;

public record MessageDto(
        Long fromUserId,
        Long toUserId,
        String content,
        long timestamp
) {}
