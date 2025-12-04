package com.collegebuddy.groups;

import com.collegebuddy.common.exceptions.ForbiddenCampusAccessException;
import com.collegebuddy.common.exceptions.InvalidConnectionActionException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.domain.*;
import com.collegebuddy.repo.GroupMemberRepository;
import com.collegebuddy.repo.GroupMessageRepository;
import com.collegebuddy.repo.GroupRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groups;
    private final GroupMemberRepository groupMembers;
    private final GroupMessageRepository groupMessages;
    private final UserRepository users;
    private final ProfileRepository profiles;

    public GroupService(GroupRepository groups,
                        GroupMemberRepository groupMembers,
                        GroupMessageRepository groupMessages,
                        UserRepository users,
                        ProfileRepository profiles) {
        this.groups = groups;
        this.groupMembers = groupMembers;
        this.groupMessages = groupMessages;
        this.users = users;
        this.profiles = profiles;
    }

    @Transactional
    public GroupDto createGroup(Long userId, String userCampus, CreateGroupRequest request) {
        log.info("Creating group: {} by user {}", request.name(), userId);

        // Create group
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setCampusDomain(userCampus);
        group.setCreatorId(userId);
        group.setVisibility(request.visibility());
        group.setCreatedAt(Instant.now());

        group = groups.save(group);

        // Add creator as admin member
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole(GroupRole.ADMIN);
        member.setJoinedAt(Instant.now());

        groupMembers.save(member);

        // Get creator info
        Profile creatorProfile = profiles.findById(userId).orElse(null);
        String creatorName = creatorProfile != null ? creatorProfile.getDisplayName() : "Unknown";

        return new GroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCampusDomain(),
                group.getCreatorId(),
                creatorName,
                group.getVisibility(),
                1,
                true,
                true,
                0, // unreadCount - new group has no messages
                group.getCreatedAt()
        );
    }

    public Page<GroupDto> getGroupsByCampus(Long userId, String userCampus, Pageable pageable) {
        Page<Group> groupPage = groups.findByCampusDomain(userCampus, pageable);

        // Get user's memberships
        Set<Long> userGroupIds = groupMembers.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());

        // Get admin memberships
        List<GroupMember> adminMemberships = groupMembers.findByUserId(userId).stream()
                .filter(m -> m.getRole() == GroupRole.ADMIN)
                .toList();
        Set<Long> adminGroupIds = adminMemberships.stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());

        // Get unread counts
        Map<Long, Long> unreadCounts = getUnreadCountsForUserGroups(userId);

        return groupPage.map(group -> toGroupDto(group, userGroupIds, adminGroupIds, unreadCounts));
    }

    public Page<GroupDto> searchGroups(Long userId, String userCampus, String query, Pageable pageable) {
        Page<Group> groupPage = groups.searchByCampus(userCampus, query, pageable);

        // Get user's memberships
        Set<Long> userGroupIds = groupMembers.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());

        Set<Long> adminGroupIds = groupMembers.findByUserId(userId).stream()
                .filter(m -> m.getRole() == GroupRole.ADMIN)
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());

        // Get unread counts
        Map<Long, Long> unreadCounts = getUnreadCountsForUserGroups(userId);

        return groupPage.map(group -> toGroupDto(group, userGroupIds, adminGroupIds, unreadCounts));
    }

    public GroupDto getGroupDetails(Long userId, Long groupId) {
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new InvalidConnectionActionException("Group not found"));

        // Get user's memberships
        Set<Long> userGroupIds = groupMembers.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());

        Set<Long> adminGroupIds = groupMembers.findByUserId(userId).stream()
                .filter(m -> m.getRole() == GroupRole.ADMIN)
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());

        // Get unread counts
        Map<Long, Long> unreadCounts = getUnreadCountsForUserGroups(userId);

        return toGroupDto(group, userGroupIds, adminGroupIds, unreadCounts);
    }

    public List<GroupMemberDto> getGroupMembers(Long userId, Long groupId) {
        // Verify group exists
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new InvalidConnectionActionException("Group not found"));

        // Get members
        List<GroupMember> members = groupMembers.findByGroupId(groupId);

        // Get user profiles
        Set<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, Profile> profileMap = profiles.findAllById(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        return members.stream()
                .map(m -> {
                    Profile p = profileMap.get(m.getUserId());
                    return new GroupMemberDto(
                            m.getUserId(),
                            p != null ? p.getDisplayName() : "Unknown",
                            p != null ? p.getAvatarUrl() : null,
                            m.getRole(),
                            m.getJoinedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public void joinGroup(Long userId, String userCampus, Long groupId) {
        log.info("User {} joining group {}", userId, groupId);

        Group group = groups.findById(groupId)
                .orElseThrow(() -> new InvalidConnectionActionException("Group not found"));

        // Verify same campus
        if (!userCampus.equalsIgnoreCase(group.getCampusDomain())) {
            throw new ForbiddenCampusAccessException("Cannot join groups from other campuses");
        }

        // Check if already a member
        if (groupMembers.existsByGroupIdAndUserId(groupId, userId)) {
            throw new InvalidConnectionActionException("Already a member of this group");
        }

        // Add member
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(GroupRole.MEMBER);
        member.setJoinedAt(Instant.now());

        groupMembers.save(member);
    }

    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        log.info("User {} leaving group {}", userId, groupId);

        Group group = groups.findById(groupId)
                .orElseThrow(() -> new InvalidConnectionActionException("Group not found"));

        // Check if member
        if (!groupMembers.existsByGroupIdAndUserId(groupId, userId)) {
            throw new InvalidConnectionActionException("Not a member of this group");
        }

        // Check if creator - creators cannot leave unless they're the last member
        if (group.getCreatorId().equals(userId)) {
            long memberCount = groupMembers.countByGroupId(groupId);
            if (memberCount > 1) {
                throw new InvalidConnectionActionException("Group creator cannot leave while other members exist. Transfer admin role or remove all members first.");
            }
        }

        groupMembers.deleteByGroupIdAndUserId(groupId, userId);
    }

    private GroupDto toGroupDto(Group group, Set<Long> userGroupIds, Set<Long> adminGroupIds, Map<Long, Long> unreadCounts) {
        long memberCount = groupMembers.countByGroupId(group.getId());

        // Get creator info
        Profile creatorProfile = profiles.findById(group.getCreatorId()).orElse(null);
        String creatorName = creatorProfile != null ? creatorProfile.getDisplayName() : "Unknown";

        // Get unread count for this group
        long unreadCount = unreadCounts.getOrDefault(group.getId(), 0L);

        return new GroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCampusDomain(),
                group.getCreatorId(),
                creatorName,
                group.getVisibility(),
                memberCount,
                userGroupIds.contains(group.getId()),
                adminGroupIds.contains(group.getId()),
                unreadCount,
                group.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<GroupMessageDto> getGroupMessages(Long userId, Long groupId) {
        log.info("Getting messages for group {} by user {}", groupId, userId);

        // Verify group exists
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Verify user is a member
        if (!groupMembers.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You must be a member to view group messages");
        }

        // Get all messages
        List<GroupMessage> messages = groupMessages.findByGroupIdOrderBySentAtAsc(groupId);

        // Convert to DTOs with sender info
        return messages.stream()
                .map(msg -> {
                    Profile senderProfile = profiles.findById(msg.getSenderId()).orElse(null);
                    String senderName = senderProfile != null ? senderProfile.getDisplayName() : "Unknown";
                    String senderAvatar = senderProfile != null ? senderProfile.getAvatarUrl() : null;

                    return new GroupMessageDto(
                            msg.getId(),
                            msg.getSenderId(),
                            senderName,
                            senderAvatar,
                            msg.getBody(),
                            msg.getSentAt()
                    );
                })
                .toList();
    }

    @Transactional
    public GroupMessageDto sendGroupMessage(Long userId, Long groupId, SendGroupMessageRequest request) {
        log.info("Sending message to group {} by user {}", groupId, userId);

        // Verify group exists
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Verify user is a member
        if (!groupMembers.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You must be a member to send messages");
        }

        // Create message
        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(userId);
        message.setBody(request.body());
        message.setSentAt(Instant.now());

        message = groupMessages.save(message);

        // Get sender profile
        Profile senderProfile = profiles.findById(userId).orElse(null);
        String senderName = senderProfile != null ? senderProfile.getDisplayName() : "Unknown";
        String senderAvatar = senderProfile != null ? senderProfile.getAvatarUrl() : null;

        return new GroupMessageDto(
                message.getId(),
                message.getSenderId(),
                senderName,
                senderAvatar,
                message.getBody(),
                message.getSentAt()
        );
    }

    /**
     * Get unread message counts for all groups the user is a member of
     * Returns a map of groupId -> unread count
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> getUnreadCountsForUserGroups(Long userId) {
        log.info("Getting unread counts for user {}", userId);

        // Get all groups user is a member of
        List<GroupMember> memberships = groupMembers.findByUserId(userId);

        Map<Long, Long> unreadCounts = new java.util.HashMap<>();

        for (GroupMember membership : memberships) {
            long count = groupMessages.countUnreadMessagesInGroup(
                    membership.getGroupId(),
                    userId,
                    membership.getLastReadMessageId()
            );

            if (count > 0) {
                unreadCounts.put(membership.getGroupId(), count);
            }
        }

        return unreadCounts;
    }

    /**
     * Mark all messages in a group as read for the current user
     * Updates the lastReadMessageId to the latest message in the group
     */
    @Transactional
    public void markGroupAsRead(Long userId, Long groupId) {
        log.info("Marking group {} as read for user {}", groupId, userId);

        // Verify user is a member
        GroupMember membership = groupMembers.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You must be a member to mark messages as read"));

        // Get the latest message ID in the group
        Optional<Long> latestMessageId = groupMessages.findLatestMessageIdByGroupId(groupId);

        if (latestMessageId.isPresent()) {
            membership.setLastReadMessageId(latestMessageId.get());
            groupMembers.save(membership);
        }
    }
}
