package com.collegebuddy.repo;

import com.collegebuddy.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUserAIdAndUserBId(Long userAId, Long userBId);

    @Modifying
    void deleteByUserAIdAndUserBId(Long userAId, Long userBId);

    @Modifying
    @SuppressWarnings("SqlResolve") // IDE cannot resolve table at design-time
    @Query(value = "INSERT INTO conversations (user_a_id, user_b_id, created_at) " +
            "VALUES (:userAId, :userBId, NOW()) " +
            "ON CONFLICT (user_a_id, user_b_id) DO NOTHING", nativeQuery = true)
    void insertIfNotExists(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    @Query("SELECT c FROM Conversation c WHERE c.userAId = :userId OR c.userBId = :userId")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);
}
