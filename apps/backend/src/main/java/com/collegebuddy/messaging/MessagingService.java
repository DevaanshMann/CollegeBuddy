package com.collegebuddy.messaging;

import com.collegebuddy.common.exceptions.MessagePermissionException;
import com.collegebuddy.domain.Connection;
import com.collegebuddy.domain.Message;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.ConversationResponse;
import com.collegebuddy.dto.MessageDto;
import com.collegebuddy.dto.SendMessageRequest;
import com.collegebuddy.repo.ConnectionRepository;
import com.collegebuddy.repo.ConversationRepository;
import com.collegebuddy.repo.MessageRepository;
import com.collegebuddy.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class MessagingService {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final ConnectionRepository connections;
    private final UserRepository users;

    public MessagingService(ConversationRepository conversations,
                            MessageRepository messages,
                            ConnectionRepository connections,
                            UserRepository users) {
        this.conversations = conversations;
        this.messages = messages;
        this.connections = connections;
        this.users = users;
    }

    public MessageDto sendMessage(Long senderId, String senderCampusDomain, SendMessageRequest req) {
        Long recipientId = req.recipientId();

        if (Objects.equals(senderId, recipientId)) {
            throw new MessagePermissionException("Cannot message yourself");
        }

        User recipient = users.findById(recipientId)
                .orElseThrow(() -> new MessagePermissionException("Recipient not found"));

        if (!senderCampusDomain.equalsIgnoreCase(recipient.getCampusDomain())) {
            throw new MessagePermissionException("Cannot message users from another campus");
        }

        long a = Math.min(senderId, recipientId);
        long b = Math.max(senderId, recipientId);

        if (!connections.existsByUserAIdAndUserBId(a, b)) {
            throw new MessagePermissionException("You must be connected to message this user");
        }

        // find or create conversation
        var convo = conversations.findByUserAIdAndUserBId(a, b)
                .orElseGet(() -> {
                    var c = new com.collegebuddy.domain.Conversation();
                    c.setUserAId(a);
                    c.setUserBId(b);
                    c.setCreatedAt(Instant.now());
                    return conversations.save(c);
                });

        Message m = new Message();
        m.setConversationId(convo.getId());
        m.setSenderId(senderId);
        m.setBody(req.body());
        m.setSentAt(Instant.now());

        Message saved = messages.save(m);

        return new MessageDto(
                saved.getId(),
                saved.getSenderId(),
                saved.getBody(),
                saved.getSentAt()
        );
    }

    public ConversationResponse getConversation(Long currentUserId, String campusDomain, Long otherUserId) {
        if (Objects.equals(currentUserId, otherUserId)) {
            throw new MessagePermissionException("Cannot load conversation with yourself");
        }

        User other = users.findById(otherUserId)
                .orElseThrow(() -> new MessagePermissionException("User not found"));

        if (!campusDomain.equalsIgnoreCase(other.getCampusDomain())) {
            throw new MessagePermissionException("Different campus");
        }

        long a = Math.min(currentUserId, otherUserId);
        long b = Math.max(currentUserId, otherUserId);

        if (!connections.existsByUserAIdAndUserBId(a, b)) {
            throw new MessagePermissionException("You must be connected to view this conversation");
        }

        var convo = conversations.findByUserAIdAndUserBId(a, b)
                .orElseGet(() -> {
                    var c = new com.collegebuddy.domain.Conversation();
                    c.setUserAId(a);
                    c.setUserBId(b);
                    c.setCreatedAt(Instant.now());
                    return conversations.save(c);
                });

        List<MessageDto> msgs = messages.findByConversationIdOrderBySentAtAsc(convo.getId())
                .stream()
                .sorted(Comparator.comparing(Message::getSentAt))
                .map(m -> new MessageDto(
                        m.getId(),
                        m.getSenderId(),
                        m.getBody(),
                        m.getSentAt()
                ))
                .toList();

        return new ConversationResponse(convo.getId(), msgs);
    }
}
