package com.collegebuddy.admin;

import com.collegebuddy.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        AccountStatus status
) {
}
