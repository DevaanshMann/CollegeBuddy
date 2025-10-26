package com.collegebuddy.dto.user;

import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(min = 1, max = 80) String displayName,
        @Size(max = 280) String bio,
        @Size(max = 500) String avatarUrl
) {}