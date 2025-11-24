package com.collegebuddy.messaging;

import com.collegebuddy.domain.Conversation;
import com.collegebuddy.repo.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class ConversationHelper {

    private static final Logger log = LoggerFactory.getLogger(ConversationHelper.class);

    private final ConversationRepository conversations;

    public ConversationHelper(ConversationRepository conversations) {
        this.conversations = conversations;
    }

    @Transactional
    public Conversation findOrCreateConversation(long userAId, long userBId) {
        log.debug("findOrCreateConversation called for userA={} and userB={}", userAId, userBId);

        // First check if exists
        var existing = conversations.findByUserAIdAndUserBId(userAId, userBId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Try native upsert first (PostgreSQL), fall back to JPA save (H2/other)
        try {
            conversations.insertIfNotExists(userAId, userBId);
        } catch (Exception e) {
            // Native query failed (likely H2), use JPA approach
            log.debug("Native upsert failed, using JPA fallback: {}", e.getMessage());
            try {
                Conversation conv = new Conversation();
                conv.setUserAId(userAId);
                conv.setUserBId(userBId);
                conv.setCreatedAt(Instant.now());
                return conversations.save(conv);
            } catch (DataIntegrityViolationException ex) {
                // Race condition - another thread created it
                log.debug("Conversation created by another thread, fetching");
            }
        }

        // Fetch the conversation (should exist now)
        return conversations.findByUserAIdAndUserBId(userAId, userBId)
                .orElseThrow(() -> new RuntimeException("Conversation should exist after insert"));
    }
}
