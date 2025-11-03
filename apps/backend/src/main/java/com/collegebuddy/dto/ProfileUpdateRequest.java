package com.collegebuddy.dto;

public record ProfileUpdateRequest(
        String displayName,
        String bio,
        String avatarUrl,
        String visibility // "PUBLIC" / "PRIVATE"
) {}
