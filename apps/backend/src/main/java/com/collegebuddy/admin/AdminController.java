package com.collegebuddy.admin;

import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * GET /admin/users
     * Get paginated list of all users
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /admin/users - Admin ID: {}", auth.id());

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<AdminUserDto> users = adminService.getAllUsers(auth.id(), pageable);

        return ResponseEntity.ok(users);
    }

    /**
     * GET /admin/users/{userId}
     * Get detailed information about a specific user
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDto> getUserDetails(@PathVariable Long userId) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /admin/users/{} - Admin ID: {}", userId, auth.id());

        AdminUserDto user = adminService.getUserDetails(auth.id(), userId);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /admin/users/{userId}/status
     * Update user account status
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("PUT /admin/users/{}/status - Admin ID: {}, New Status: {}",
                 userId, auth.id(), request.status());

        adminService.updateUserStatus(auth.id(), userId, request.status());

        return ResponseEntity.ok(Map.of(
                "message", "User status updated successfully",
                "userId", userId,
                "newStatus", request.status()
        ));
    }

    /**
     * GET /admin/stats
     * Get platform statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("GET /admin/stats - Admin ID: {}", auth.id());

        AdminStatsDto stats = adminService.getStats(auth.id());
        return ResponseEntity.ok(stats);
    }
}
