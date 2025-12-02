package com.collegebuddy.repo;

import com.collegebuddy.domain.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    /**
     * Find a specific block relationship
     */
    Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Get all users blocked by a specific user
     */
    List<BlockedUser> findByBlockerId(Long blockerId);

    /**
     * Get all users who have blocked a specific user
     */
    List<BlockedUser> findByBlockedId(Long blockedId);

    /**
     * Check if userA has blocked userB
     */
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Check if there's any block between two users (either direction)
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlockedUser b " +
           "WHERE (b.blockerId = :userId1 AND b.blockedId = :userId2) " +
           "OR (b.blockerId = :userId2 AND b.blockedId = :userId1)")
    boolean existsBlockBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Delete a block relationship
     */
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Count how many users a specific user has blocked
     */
    long countByBlockerId(Long blockerId);
}
