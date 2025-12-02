package com.collegebuddy.blocking;

import com.collegebuddy.common.exceptions.BlockAlreadyExistsException;
import com.collegebuddy.common.exceptions.BlockNotFoundException;
import com.collegebuddy.common.exceptions.InvalidBlockActionException;
import com.collegebuddy.common.exceptions.UserNotFoundException;
import com.collegebuddy.domain.BlockedUser;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.BlockedUserDto;
import com.collegebuddy.repo.BlockedUserRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlockingService {

    private static final Logger log = LoggerFactory.getLogger(BlockingService.class);

    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public BlockingService(BlockedUserRepository blockedUserRepository,
                          UserRepository userRepository,
                          ProfileRepository profileRepository) {
        this.blockedUserRepository = blockedUserRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Block a user
     */
    @Transactional
    public void blockUser(Long blockerId, Long userIdToBlock) {
        log.info("User {} attempting to block user {}", blockerId, userIdToBlock);

        // Validation
        if (blockerId.equals(userIdToBlock)) {
            throw new InvalidBlockActionException("Cannot block yourself");
        }

        // Verify both users exist
        if (!userRepository.existsById(blockerId)) {
            throw new UserNotFoundException("Blocker user not found");
        }
        if (!userRepository.existsById(userIdToBlock)) {
            throw new UserNotFoundException("User to block not found");
        }

        // Check if already blocked
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, userIdToBlock)) {
            throw new BlockAlreadyExistsException("User is already blocked");
        }

        // Create block
        BlockedUser block = new BlockedUser(blockerId, userIdToBlock);
        blockedUserRepository.save(block);

        log.info("User {} successfully blocked user {}", blockerId, userIdToBlock);
    }

    /**
     * Unblock a user
     */
    @Transactional
    public void unblockUser(Long blockerId, Long userIdToUnblock) {
        log.info("User {} attempting to unblock user {}", blockerId, userIdToUnblock);

        BlockedUser block = blockedUserRepository
                .findByBlockerIdAndBlockedId(blockerId, userIdToUnblock)
                .orElseThrow(() -> new BlockNotFoundException("User is not blocked"));

        blockedUserRepository.delete(block);

        log.info("User {} successfully unblocked user {}", blockerId, userIdToUnblock);
    }

    /**
     * Get all users blocked by the current user
     */
    @Transactional(readOnly = true)
    public List<BlockedUserDto> getBlockedUsers(Long blockerId) {
        log.debug("Fetching blocked users for user {}", blockerId);

        List<BlockedUser> blocked = blockedUserRepository.findByBlockerId(blockerId);

        return blocked.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if userA has blocked userB
     */
    public boolean isBlocked(Long userAId, Long userBId) {
        return blockedUserRepository.existsByBlockerIdAndBlockedId(userAId, userBId);
    }

    /**
     * Check if there's a block between two users (either direction)
     */
    public boolean isBlockBetween(Long userId1, Long userId2) {
        return blockedUserRepository.existsBlockBetween(userId1, userId2);
    }

    /**
     * Convert BlockedUser entity to DTO with profile information
     */
    private BlockedUserDto toDto(BlockedUser blocked) {
        Long blockedUserId = blocked.getBlockedId();
        Profile profile = profileRepository.findById(blockedUserId).orElse(null);

        return new BlockedUserDto(
                blocked.getId(),
                blockedUserId,
                profile != null ? profile.getDisplayName() : "Unknown User",
                profile != null ? profile.getAvatarUrl() : null,
                blocked.getCreatedAt()
        );
    }
}
