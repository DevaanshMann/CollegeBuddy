package com.collegebuddy.repo;

import com.collegebuddy.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    @Modifying
    void deleteByConversationId(Long conversationId);
}
