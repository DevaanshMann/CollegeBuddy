package com.collegebuddy.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {}
