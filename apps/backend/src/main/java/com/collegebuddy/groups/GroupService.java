package com.collegebuddy.groups;

import com.collegebuddy.common.exceptions.ForbiddenCampusAccessException;
import com.collegebuddy.common.exceptions.InvalidConnectionActionException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.domain.*;
import com.collegebuddy.repo.GroupMemberRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groups;
    private final GroupMemberRepository groupMembers;
    private final UserRepository users;
    private final ProfileRepository profiles;

    public GroupService(GroupRepository groups,
                        GroupMemberRepository groupMembers,
                        UserRepository users,
                        ProfileRepository profiles) {
        this.groups = groups;
        this.groupMembers = groupMembers;
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

        return groupPage.map(group -> toGroupDto(group, userGroupIds, adminGroupIds));
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

        return groupPage.map(group -> toGroupDto(group, userGroupIds, adminGroupIds));
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

        return toGroupDto(group, userGroupIds, adminGroupIds);
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

    private GroupDto toGroupDto(Group group, Set<Long> userGroupIds, Set<Long> adminGroupIds) {
        long memberCount = groupMembers.countByGroupId(group.getId());

        // Get creator info
        Profile creatorProfile = profiles.findById(group.getCreatorId()).orElse(null);
        String creatorName = creatorProfile != null ? creatorProfile.getDisplayName() : "Unknown";

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
                group.getCreatedAt()
        );
    }
}
