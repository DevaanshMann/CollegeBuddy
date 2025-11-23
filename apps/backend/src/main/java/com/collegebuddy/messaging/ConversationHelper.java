package com.collegebuddy.messaging;

import com.collegebuddy.domain.Conversation;
import com.collegebuddy.repo.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

        // Use native SQL upsert - won't throw exception if already exists
        conversations.insertIfNotExists(userAId, userBId);

        // Now fetch the conversation (guaranteed to exist)
        return conversations.findByUserAIdAndUserBId(userAId, userBId)
                .orElseThrow(() -> new RuntimeException("Conversation should exist after insert"));
    }
}
