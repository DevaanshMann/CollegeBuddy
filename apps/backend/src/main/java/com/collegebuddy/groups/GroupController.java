package com.collegebuddy.groups;

import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * POST /groups
     * Create a new group
     */
    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("POST /groups - User: {}, Group: {}", auth.id(), request.name());

        GroupDto group = groupService.createGroup(auth.id(), auth.campusDomain(), request);
        return ResponseEntity.ok(group);
    }

    /**
     * GET /groups
     * Get all groups for the user's campus
     */
    @GetMapping
    public ResponseEntity<Page<GroupDto>> getGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /groups - User: {}, Page: {}, Search: {}", auth.id(), page, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<GroupDto> groups;
        if (search != null && !search.trim().isEmpty()) {
            groups = groupService.searchGroups(auth.id(), auth.campusDomain(), search, pageable);
        } else {
            groups = groupService.getGroupsByCampus(auth.id(), auth.campusDomain(), pageable);
        }

        return ResponseEntity.ok(groups);
    }

    /**
     * GET /groups/{groupId}
     * Get group details
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupDetails(@PathVariable Long groupId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /groups/{} - User: {}", groupId, auth.id());

        GroupDto group = groupService.getGroupDetails(auth.id(), groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * GET /groups/{groupId}/members
     * Get group members
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(@PathVariable Long groupId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /groups/{}/members - User: {}", groupId, auth.id());

        List<GroupMemberDto> members = groupService.getGroupMembers(auth.id(), groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * POST /groups/{groupId}/join
     * Join a group
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable Long groupId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("POST /groups/{}/join - User: {}", groupId, auth.id());

        groupService.joinGroup(auth.id(), auth.campusDomain(), groupId);
        return ResponseEntity.ok(Map.of("message", "Joined group successfully"));
    }

    /**
     * POST /groups/{groupId}/leave
     * Leave a group
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("POST /groups/{}/leave - User: {}", groupId, auth.id());

        groupService.leaveGroup(auth.id(), groupId);
        return ResponseEntity.ok(Map.of("message", "Left group successfully"));
    }

    /**
     * GET /groups/{groupId}/messages
     * Get all messages in a group
     */
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<GroupMessageDto>> getGroupMessages(@PathVariable Long groupId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /groups/{}/messages - User: {}", groupId, auth.id());

        List<GroupMessageDto> messages = groupService.getGroupMessages(auth.id(), groupId);
        return ResponseEntity.ok(messages);
    }

    /**
     * POST /groups/{groupId}/messages
     * Send a message to a group
     */
    @PostMapping("/{groupId}/messages")
    public ResponseEntity<GroupMessageDto> sendGroupMessage(
            @PathVariable Long groupId,
            @Valid @RequestBody SendGroupMessageRequest request
    ) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("POST /groups/{}/messages - User: {}", groupId, auth.id());

        GroupMessageDto message = groupService.sendGroupMessage(auth.id(), groupId, request);
        return ResponseEntity.ok(message);
    }

    /**
     * POST /groups/{groupId}/mark-read
     * Mark all messages in a group as read
     */
    @PostMapping("/{groupId}/mark-read")
    public ResponseEntity<Void> markGroupAsRead(@PathVariable Long groupId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("POST /groups/{}/mark-read - User: {}", groupId, auth.id());

        groupService.markGroupAsRead(auth.id(), groupId);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /groups/unread-counts
     * Get unread message counts for all user's groups
     */
    @GetMapping("/unread-counts")
    public ResponseEntity<Map<Long, Long>> getUnreadCounts() {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /groups/unread-counts - User: {}", auth.id());

        Map<Long, Long> unreadCounts = groupService.getUnreadCountsForUserGroups(auth.id());
        return ResponseEntity.ok(unreadCounts);
    }
}
