package com.collegebuddy.admin;

import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.common.exceptions.UserNotFoundException;
import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.Role;
import com.collegebuddy.domain.User;
import com.collegebuddy.repo.ConnectionRepository;
import com.collegebuddy.repo.MessageRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final ConnectionRepository connections;
    private final MessageRepository messages;

    public AdminService(UserRepository users,
                        ProfileRepository profiles,
                        ConnectionRepository connections,
                        MessageRepository messages) {
        this.users = users;
        this.profiles = profiles;
        this.connections = connections;
        this.messages = messages;
    }

    /**
     * Verify that the current user is an admin
     */
    private void verifyAdmin(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            log.warn("Non-admin user {} attempted to access admin functionality", userId);
            throw new UnauthorizedException("Admin access required");
        }
    }

    /**
     * Get paginated list of all users
     */
    public Page<AdminUserDto> getAllUsers(Long adminId, Pageable pageable) {
        verifyAdmin(adminId);

        Page<User> userPage = users.findAll(pageable);

        return userPage.map(user -> {
            Profile profile = profiles.findById(user.getId()).orElse(null);
            return new AdminUserDto(
                    user.getId(),
                    user.getEmail(),
                    profile != null ? profile.getDisplayName() : "N/A",
                    user.getCampusDomain(),
                    user.getStatus(),
                    user.getRole(),
                    profile != null ? profile.getAvatarUrl() : null,
                    null // createdAt not in User entity currently
            );
        });
    }

    /**
     * Get detailed information about a specific user
     */
    public AdminUserDto getUserDetails(Long adminId, Long targetUserId) {
        verifyAdmin(adminId);

        User user = users.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Profile profile = profiles.findById(user.getId()).orElse(null);

        return new AdminUserDto(
                user.getId(),
                user.getEmail(),
                profile != null ? profile.getDisplayName() : "N/A",
                user.getCampusDomain(),
                user.getStatus(),
                user.getRole(),
                profile != null ? profile.getAvatarUrl() : null,
                null
        );
    }

    /**
     * Update user account status
     */
    @Transactional
    public void updateUserStatus(Long adminId, Long targetUserId, AccountStatus newStatus) {
        verifyAdmin(adminId);

        User user = users.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN && !adminId.equals(targetUserId)) {
            throw new UnauthorizedException("Cannot modify other admin accounts");
        }

        log.info("Admin {} updating user {} status from {} to {}",
                 adminId, targetUserId, user.getStatus(), newStatus);

        user.setStatus(newStatus);
        users.save(user);
    }

    /**
     * Update user role
     */
    @Transactional
    public void updateUserRole(Long adminId, Long targetUserId, Role newRole) {
        verifyAdmin(adminId);

        User user = users.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN && !adminId.equals(targetUserId)) {
            throw new UnauthorizedException("Cannot modify other admin accounts");
        }

        log.info("Admin {} updating user {} role from {} to {}",
                 adminId, targetUserId, user.getRole(), newRole);

        user.setRole(newRole);
        users.save(user);
    }

    /**
     * Get platform statistics
     */
    public AdminStatsDto getStats(Long adminId) {
        verifyAdmin(adminId);

        long totalUsers = users.count();
        long activeUsers = users.countByStatus(AccountStatus.ACTIVE);
        long pendingUsers = users.countByStatus(AccountStatus.PENDING_VERIFICATION);
        long deactivatedUsers = users.countByStatus(AccountStatus.DEACTIVATED);
        long totalConnections = connections.count();
        long totalMessages = messages.count();

        return new AdminStatsDto(
                totalUsers,
                activeUsers,
                pendingUsers,
                deactivatedUsers,
                totalConnections,
                totalMessages
        );
    }
}
