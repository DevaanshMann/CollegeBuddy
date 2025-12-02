package com.collegebuddy.account;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "Password is required to confirm account deletion")
        String password
) {
}
