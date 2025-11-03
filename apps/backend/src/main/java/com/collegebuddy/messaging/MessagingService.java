package com.collegebuddy.messaging;

import com.collegebuddy.dto.ConversationDto;
import com.collegebuddy.dto.MessageDto;
import com.collegebuddy.dto.SendMessageRequest;
import org.springframework.stereotype.Service;

/**
 * Conversation creation/loading, message persistence, unread counts.
 */
@Service
public class MessagingService {

    private final DeliveryService deliveryService;

    public MessagingService(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    public MessageDto send(SendMessageRequest request) {
        // TODO: persist message, then call deliveryService.deliver(...)
        return new MessageDto(1L, 2L, request.content(), System.currentTimeMillis());
    }

    public ConversationDto loadConversation(Long userId, Long otherUserId) {
        // TODO: load all messages between userId and otherUserId
        return new ConversationDto();
    }
}
