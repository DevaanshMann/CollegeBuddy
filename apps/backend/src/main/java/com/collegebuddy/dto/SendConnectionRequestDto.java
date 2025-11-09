package com.collegebuddy.dto;

public record SendConnectionRequestDto(
        Long toUserId,
        String message
) {}
