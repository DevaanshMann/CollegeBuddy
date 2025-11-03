package com.collegebuddy.repo;

import com.collegebuddy.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUserAIdAndUserBId(Long userAId, Long userBId);
}
