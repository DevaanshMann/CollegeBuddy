package com.collegebuddy.dto;

public record ProfileResponse(
        String displayName,
        String bio,
        String avatarUrl,
        String visibility
) {}
