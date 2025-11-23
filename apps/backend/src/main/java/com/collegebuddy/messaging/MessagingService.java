package com.collegebuddy.messaging;

import com.collegebuddy.common.exceptions.MessagePermissionException;
import com.collegebuddy.domain.Message;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.ConversationResponse;
import com.collegebuddy.dto.MessageDto;
import com.collegebuddy.dto.SendMessageRequest;
import com.collegebuddy.repo.ConnectionRepository;
import com.collegebuddy.repo.ConversationRepository;
import com.collegebuddy.repo.MessageRepository;
import com.collegebuddy.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final ConnectionRepository connections;
    private final UserRepository users;
    private final ConversationHelper conversationHelper;

    public MessagingService(ConversationRepository conversations,
                            MessageRepository messages,
                            ConnectionRepository connections,
                            UserRepository users,
                            ConversationHelper conversationHelper) {
        this.conversations = conversations;
        this.messages = messages;
        this.connections = connections;
        this.users = users;
        this.conversationHelper = conversationHelper;
    }

    @Transactional
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
        var convo = conversationHelper.findOrCreateConversation(a, b);

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

    @Transactional
    public ConversationResponse getConversation(Long currentUserId, String campusDomain, Long otherUserId) {
        log.debug("getConversation called: currentUserId={}, campusDomain={}, otherUserId={}",
                currentUserId, campusDomain, otherUserId);

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

        log.debug("Checking connection between userA={} and userB={}", a, b);
        boolean connected = connections.existsByUserAIdAndUserBId(a, b);
        log.debug("Connection exists: {}", connected);

        if (!connected) {
            throw new MessagePermissionException("You must be connected to view this conversation");
        }

        log.debug("Looking for existing conversation");
        var convo = conversationHelper.findOrCreateConversation(a, b);

        log.debug("Conversation found/created with id={}", convo.getId());

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

        log.debug("Returning conversation with {} messages", msgs.size());
        return new ConversationResponse(convo.getId(), msgs);
    }
}
