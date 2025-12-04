package com.collegebuddy.repo;

import com.collegebuddy.domain.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    @Query("SELECT m FROM GroupMessage m WHERE m.groupId = :groupId ORDER BY m.sentAt ASC")
    List<GroupMessage> findByGroupIdOrderBySentAtAsc(@Param("groupId") Long groupId);

    /**
     * Get the latest message ID for a group
     */
    @Query("SELECT m.id FROM GroupMessage m WHERE m.groupId = :groupId ORDER BY m.sentAt DESC LIMIT 1")
    Optional<Long> findLatestMessageIdByGroupId(@Param("groupId") Long groupId);

    /**
     * Count unread messages in a group for a specific user
     * Counts messages sent after the user's last read message (or all messages if never read)
     */
    @Query("SELECT COUNT(m) FROM GroupMessage m WHERE m.groupId = :groupId " +
           "AND (:lastReadMessageId IS NULL OR m.id > :lastReadMessageId) " +
           "AND m.senderId != :userId")
    long countUnreadMessagesInGroup(@Param("groupId") Long groupId,
                                     @Param("userId") Long userId,
                                     @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * Get the last message in a group
     */
    @Query("SELECT m FROM GroupMessage m WHERE m.groupId = :groupId ORDER BY m.sentAt DESC LIMIT 1")
    Optional<GroupMessage> findLastMessageByGroupId(@Param("groupId") Long groupId);
}
