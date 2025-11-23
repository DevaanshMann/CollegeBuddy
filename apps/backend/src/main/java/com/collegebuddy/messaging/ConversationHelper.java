package com.collegebuddy.messaging;

import com.collegebuddy.domain.Conversation;
import com.collegebuddy.repo.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ConversationHelper {

    private static final Logger log = LoggerFactory.getLogger(ConversationHelper.class);

    private final ConversationRepository conversations;

    public ConversationHelper(ConversationRepository conversations) {
        this.conversations = conversations;
    }

    public Conversation findOrCreateConversation(long userAId, long userBId) {
        // First try to find existing
        var existing = conversations.findByUserAIdAndUserBId(userAId, userBId);
        if (existing.isPresent()) {
            log.debug("Found existing conversation for userA={} and userB={}", userAId, userBId);
            return existing.get();
        }

        // Create new conversation
        try {
            log.debug("Creating new conversation for userA={} and userB={}", userAId, userBId);
            var c = new Conversation();
            c.setUserAId(userAId);
            c.setUserBId(userBId);
            c.setCreatedAt(Instant.now());
            return conversations.save(c);
        } catch (DataIntegrityViolationException e) {
            // Conversation already exists (race condition or cache issue) - fetch it
            log.debug("Conversation already exists, fetching for userA={} and userB={}", userAId, userBId);
            return conversations.findByUserAIdAndUserBId(userAId, userBId)
                    .orElseThrow(() -> new RuntimeException("Conversation should exist after constraint violation"));
        }
    }
}
