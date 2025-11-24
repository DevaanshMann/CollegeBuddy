package com.collegebuddy.repo;

import com.collegebuddy.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    @Modifying
    void deleteByConversationId(Long conversationId);

    // Count unread messages in a conversation for a specific user (messages not sent by them and not read)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId " +
           "AND m.senderId != :userId AND m.readAt IS NULL")
    long countUnreadInConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    // Mark all messages in a conversation as read (for messages not sent by the current user)
    @Modifying
    @Query("UPDATE Message m SET m.readAt = :readAt " +
           "WHERE m.conversationId = :conversationId AND m.senderId != :userId AND m.readAt IS NULL")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("readAt") java.time.Instant readAt);
}
