package com.collegebuddy.dto;

public record SignupRequest(
        String email,
        String password,
        String campusDomain
) {}
