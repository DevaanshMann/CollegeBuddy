package com.collegebuddy.dto;

import java.util.List;

/**
 * Represents the conversation (history of messages).
 */
public record ConversationDto(
        List<MessageDto> messages
) {
    public ConversationDto() {
        this(List.of());
    }
}
