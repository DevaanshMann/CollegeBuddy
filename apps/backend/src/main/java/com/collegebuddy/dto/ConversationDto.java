package com.collegebuddy.dto;

import java.util.List;

public record ConversationDto(
        List<MessageDto> messages
) {
    public ConversationDto() {
        this(List.of());
    }
}
