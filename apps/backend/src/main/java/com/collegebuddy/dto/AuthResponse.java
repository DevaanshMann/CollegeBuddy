package com.collegebuddy.dto;

public record AuthResponse(
        String status,
        String jwt
) {}
