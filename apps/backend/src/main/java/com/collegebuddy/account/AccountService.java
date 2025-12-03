package com.collegebuddy.account;

import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.media.MediaStorageService;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;
    private final MediaStorageService mediaStorage;

    public AccountService(UserRepository users,
                          ProfileRepository profiles,
                          PasswordEncoder passwordEncoder,
                          MediaStorageService mediaStorage) {
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
        this.mediaStorage = mediaStorage;
    }

    @Transactional
    public void deleteAccount(Long userId, String password) {
        log.info("Delete account requested for user ID: {}", userId);

        // Find user
        User user = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            log.warn("Failed account deletion attempt - incorrect password for user ID: {}", userId);
            throw new UnauthorizedException("Incorrect password");
        }

        // Delete avatar file if exists
        Profile profile = profiles.findById(userId).orElse(null);
        if (profile != null && profile.getAvatarUrl() != null) {
            try {
                mediaStorage.deleteFile(profile.getAvatarUrl());
                log.info("Deleted avatar for user ID: {}", userId);
            } catch (Exception e) {
                log.error("Failed to delete avatar for user ID: {}, continuing with account deletion", userId, e);
                // Continue with account deletion even if avatar deletion fails
            }
        }

        // Delete user (CASCADE will delete all related data)
        users.delete(user);
        log.info("Successfully deleted account for user ID: {}", userId);
    }
}
