package com.collegebuddy.dto;

public record LoginRequest(
        String email,
        String password
) {}
