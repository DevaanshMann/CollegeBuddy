package com.collegebuddy.dto.user;

public record UserSearchResponseDto(
        Long id,
        String displayName,
        String school,
        Integer mutualCount,
        boolean isFriend
) {}