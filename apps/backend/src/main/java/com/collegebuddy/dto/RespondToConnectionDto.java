package com.collegebuddy.dto;

public record RespondToConnectionDto(
        Long requestId,
        String decision // "ACCEPT" or "DECLINE"
) {}
