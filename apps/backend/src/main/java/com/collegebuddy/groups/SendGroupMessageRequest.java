package com.collegebuddy.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendGroupMessageRequest(
        @NotBlank(message = "Message body cannot be empty")
        @Size(max = 5000, message = "Message is too long")
        String body
) {}
