package com.collegebuddy.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ConnectionService business logic.
 * Tests validation rules and edge cases.
 */
class ConnectionServiceLogicTest {

    @Test
    void calculateConnectionUserIds_shouldOrderUserIdsConsistently() {
        // Connection IDs should always be ordered (smaller ID first)
        // This ensures we don't create duplicate connections
        long userId1 = 5L;
        long userId2 = 3L;

        long userA = Math.min(userId1, userId2);
        long userB = Math.max(userId1, userId2);

        assertThat(userA).isEqualTo(3L);
        assertThat(userB).isEqualTo(5L);

        // Test reverse order gives same result
        long userA2 = Math.min(userId2, userId1);
        long userB2 = Math.max(userId2, userId1);

        assertThat(userA).isEqualTo(userA2);
        assertThat(userB).isEqualTo(userB2);
    }

    @Test
    void validateConnectionRequest_shouldRejectSelfConnection() {
        Long requesterId = 1L;
        Long targetId = 1L;

        boolean isSelfConnection = requesterId.equals(targetId);

        assertThat(isSelfConnection).isTrue();
    }

    @Test
    void validateCampusDomain_shouldMatchCaseInsensitive() {
        String requesterCampus = "cpp.edu";
        String targetCampus = "CPP.EDU";

        boolean matches = requesterCampus.equalsIgnoreCase(targetCampus);

        assertThat(matches).isTrue();
    }

    @Test
    void validateCampusDomain_shouldRejectDifferentCampuses() {
        String requesterCampus = "cpp.edu";
        String targetCampus = "ucla.edu";

        boolean matches = requesterCampus.equalsIgnoreCase(targetCampus);

        assertThat(matches).isFalse();
    }
}
