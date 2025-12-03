package com.collegebuddy.dto;

import java.time.Instant;

public record ConversationListItemDto(
        Long otherUserId,
        String otherUserName,
        String otherUserAvatar,
        String lastMessage,
        Instant lastMessageTime,
        Long unreadCount
) {}
