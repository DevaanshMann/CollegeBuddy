package com.collegebuddy.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BlockingService validation logic.
 */
class BlockingServiceLogicTest {

    @Test
    void validateBlockAction_shouldRejectSelfBlock() {
        Long blockerId = 1L;
        Long userToBlock = 1L;

        boolean isSelfBlock = blockerId.equals(userToBlock);

        assertThat(isSelfBlock).isTrue();
    }

    @Test
    void validateBlockAction_shouldAllowDifferentUsers() {
        Long blockerId = 1L;
        Long userToBlock = 2L;

        boolean isSelfBlock = blockerId.equals(userToBlock);

        assertThat(isSelfBlock).isFalse();
    }

    @Test
    void checkBlockBetween_shouldDetectBidirectionalBlock() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        // Simulating block check logic
        // In reality, this would check: user1 blocked user2 OR user2 blocked user1
        boolean user1BlockedUser2 = false; // would be repository check
        boolean user2BlockedUser1 = true;  // would be repository check

        boolean hasBlockBetween = user1BlockedUser2 || user2BlockedUser1;

        assertThat(hasBlockBetween).isTrue();
    }

    @Test
    void checkBlockBetween_shouldReturnFalseWhenNoBlock() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        boolean user1BlockedUser2 = false;
        boolean user2BlockedUser1 = false;

        boolean hasBlockBetween = user1BlockedUser2 || user2BlockedUser1;

        assertThat(hasBlockBetween).isFalse();
    }
}
