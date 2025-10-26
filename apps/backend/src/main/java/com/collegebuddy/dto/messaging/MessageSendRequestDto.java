package com.collegebuddy.dto.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequestDto(
        @NotBlank @Size(max = 4000) String text
) {}