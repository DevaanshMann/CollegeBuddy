package com.collegebuddy.dto;

public record SendMessageRequest(
        Long toUserId,
        String content
) {}
