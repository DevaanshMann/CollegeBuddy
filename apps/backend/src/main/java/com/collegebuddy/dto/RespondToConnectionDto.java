package com.collegebuddy.dto;

public record RespondToConnectionDto(
        Long requestId,
        String action // "ACCEPT", "DECLINE"
) {}
