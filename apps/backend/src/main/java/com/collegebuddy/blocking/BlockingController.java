package com.collegebuddy.blocking;

import com.collegebuddy.dto.BlockUserRequest;
import com.collegebuddy.dto.BlockedUserDto;
import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blocked-users")
public class BlockingController {

    private final BlockingService blockingService;

    public BlockingController(BlockingService blockingService) {
        this.blockingService = blockingService;
    }

    /**
     * Block a user
     * POST /blocked-users
     */
    @PostMapping
    public ResponseEntity<Void> blockUser(@Valid @RequestBody BlockUserRequest request) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        blockingService.blockUser(current.id(), request.userId());
        return ResponseEntity.ok().build();
    }

    /**
     * Unblock a user
     * DELETE /blocked-users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        blockingService.unblockUser(current.id(), userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get list of blocked users
     * GET /blocked-users
     */
    @GetMapping
    public ResponseEntity<List<BlockedUserDto>> getBlockedUsers() {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        List<BlockedUserDto> blocked = blockingService.getBlockedUsers(current.id());
        return ResponseEntity.ok(blocked);
    }

    /**
     * Check if a user is blocked
     * GET /blocked-users/check/{userId}
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> isBlocked(@PathVariable Long userId) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        boolean blocked = blockingService.isBlocked(current.id(), userId);
        return ResponseEntity.ok(blocked);
    }
}
