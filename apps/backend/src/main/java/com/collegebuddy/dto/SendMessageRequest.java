package com.collegebuddy.dto;

public record SendMessageRequest(
        Long recipientId,
        String body
) {}
