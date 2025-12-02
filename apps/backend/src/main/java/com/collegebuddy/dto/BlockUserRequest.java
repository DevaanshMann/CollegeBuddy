package com.collegebuddy.dto;

import jakarta.validation.constraints.NotNull;

public record BlockUserRequest(
    @NotNull(message = "User ID is required")
    Long userId
) {}
