package com.collegebuddy.messaging;

import com.collegebuddy.dto.ConversationDto;
import com.collegebuddy.dto.MessageDto;
import com.collegebuddy.dto.SendMessageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 1:1 messaging between connected users.
 */
@RestController
@RequestMapping("/messages")
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody SendMessageRequest request) {
        // TODO: ensure users are connected, persist message, deliver
        return ResponseEntity.ok(new MessageDto(1L, 2L, "hello", System.currentTimeMillis()));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable Long otherUserId) {
        // TODO: load conversation between auth user + otherUserId
        return ResponseEntity.ok(new ConversationDto());
    }
}
