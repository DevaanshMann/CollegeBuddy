package com.collegebuddy.dto;

public record SendConnectionRequestDto(
        Long fromUserId,
        Long toUserId
) {}
