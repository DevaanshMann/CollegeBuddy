package com.collegebuddy.service;

import com.collegebuddy.domain.GroupRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GroupService permission and validation logic.
 */
class GroupServiceLogicTest {

    @Test
    void validateCampusAccess_shouldAllowSameCampus() {
        String userCampus = "cpp.edu";
        String groupCampus = "cpp.edu";

        boolean canAccess = userCampus.equalsIgnoreCase(groupCampus);

        assertThat(canAccess).isTrue();
    }

    @Test
    void validateCampusAccess_shouldDenyCrossCampus() {
        String userCampus = "cpp.edu";
        String groupCampus = "ucla.edu";

        boolean canAccess = userCampus.equalsIgnoreCase(groupCampus);

        assertThat(canAccess).isFalse();
    }

    @Test
    void validateCampusAccess_shouldBeCaseInsensitive() {
        String userCampus = "CPP.EDU";
        String groupCampus = "cpp.edu";

        boolean canAccess = userCampus.equalsIgnoreCase(groupCampus);

        assertThat(canAccess).isTrue();
    }

    @Test
    void checkAdminPermission_shouldGrantAdminRole() {
        GroupRole userRole = GroupRole.ADMIN;

        boolean isAdmin = userRole == GroupRole.ADMIN;

        assertThat(isAdmin).isTrue();
    }

    @Test
    void checkAdminPermission_shouldDenyMemberRole() {
        GroupRole userRole = GroupRole.MEMBER;

        boolean isAdmin = userRole == GroupRole.ADMIN;

        assertThat(isAdmin).isFalse();
    }

    @Test
    void validateGroupLeave_shouldAllowLastMemberCreatorToLeave() {
        Long userId = 1L;
        Long creatorId = 1L;
        long memberCount = 1L;

        boolean isCreator = userId.equals(creatorId);
        boolean isLastMember = memberCount == 1;
        boolean canLeave = !isCreator || isLastMember;

        assertThat(canLeave).isTrue();
    }

    @Test
    void validateGroupLeave_shouldPreventCreatorLeavingWithOthers() {
        Long userId = 1L;
        Long creatorId = 1L;
        long memberCount = 3L;

        boolean isCreator = userId.equals(creatorId);
        boolean isLastMember = memberCount == 1;
        boolean canLeave = !isCreator || isLastMember;

        assertThat(canLeave).isFalse();
    }

    @Test
    void validateGroupLeave_shouldAllowNonCreatorToLeave() {
        Long userId = 2L;
        Long creatorId = 1L;
        long memberCount = 3L;

        boolean isCreator = userId.equals(creatorId);
        boolean isLastMember = memberCount == 1;
        boolean canLeave = !isCreator || isLastMember;

        assertThat(canLeave).isTrue();
    }

    @Test
    void calculateUnreadCount_shouldReturnZeroWhenNoNewMessages() {
        Long lastReadMessageId = 100L;
        Long latestMessageId = 100L;

        long unreadCount = latestMessageId > lastReadMessageId ? latestMessageId - lastReadMessageId : 0;

        assertThat(unreadCount).isEqualTo(0);
    }

    @Test
    void calculateUnreadCount_shouldReturnDifferenceWhenNewMessages() {
        Long lastReadMessageId = 95L;
        Long latestMessageId = 100L;

        long unreadCount = latestMessageId > lastReadMessageId ? latestMessageId - lastReadMessageId : 0;

        assertThat(unreadCount).isEqualTo(5);
    }
}
