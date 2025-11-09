package com.collegebuddy.dto;

import java.util.List;

public record ConversationResponse(
        Long conversationId,
        List<MessageDto> messages
) {}
