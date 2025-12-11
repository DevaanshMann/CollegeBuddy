package com.collegebuddy.admin;

import com.collegebuddy.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {
}
