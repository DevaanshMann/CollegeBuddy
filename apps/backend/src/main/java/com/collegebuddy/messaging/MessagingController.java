package com.collegebuddy.messaging;

import com.collegebuddy.dto.ConversationListItemDto;
import com.collegebuddy.dto.ConversationResponse;
import com.collegebuddy.dto.MessageDto;
import com.collegebuddy.dto.SendMessageRequest;
import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationListItemDto>> getAllConversations() {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        List<ConversationListItemDto> conversations = messagingService.getAllConversations(
                current.id(),
                current.campusDomain()
        );
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDto> send(@RequestBody SendMessageRequest request) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        MessageDto msg = messagingService.sendMessage(current.id(), current.campusDomain(), request);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long otherUserId) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        ConversationResponse resp = messagingService.getConversation(
                current.id(),
                current.campusDomain(),
                otherUserId
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/mark-read/{otherUserId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long otherUserId) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        messagingService.markConversationAsRead(current.id(), otherUserId);
        return ResponseEntity.ok().build();
    }
}
