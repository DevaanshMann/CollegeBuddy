package com.collegebuddy.profile;

import com.collegebuddy.common.exceptions.ProfileVisibilityException;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.ProfileResponse;
import com.collegebuddy.dto.ProfileUpdateRequest;
import com.collegebuddy.media.MediaStorageService;
import com.collegebuddy.repo.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profiles;
    private final MediaStorageService mediaStorage;

    public ProfileService(ProfileRepository profiles, MediaStorageService mediaStorage) {
        this.profiles = profiles;
        this.mediaStorage = mediaStorage;
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        Visibility visibility = parseVisibility(request.visibility());

        Profile profile = profiles.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUserId(userId);
            return p;
        });

        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setVisibility(visibility);

        Profile saved = profiles.save(profile);

        return toResponse(saved);
    }

    public ProfileResponse getProfile(Long targetUserId, Long requesterUserId) {
        Optional<Profile> opt = profiles.findById(targetUserId);
        if (opt.isEmpty()) {
            throw new ProfileVisibilityException("Profile not found");
        }

        Profile profile = opt.get();

        if (profile.getVisibility() == Visibility.PRIVATE && !targetUserId.equals(requesterUserId)) {
            throw new ProfileVisibilityException("Profile is private");
        }

        return toResponse(profile);
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        // Store the file using media storage strategy
        String avatarUrl = mediaStorage.storeAvatar(file, userId);

        // Update user's profile with new avatar URL
        profiles.findById(userId).ifPresent(profile -> {
            // Delete old avatar file if it exists
            String oldAvatarUrl = profile.getAvatarUrl();
            if (oldAvatarUrl != null) {
                boolean deleted = mediaStorage.deleteFile(oldAvatarUrl);
                if (deleted) {
                    log.info("Deleted old avatar for user {}: {}", userId, oldAvatarUrl);
                }
            }

            // Save new avatar URL
            profile.setAvatarUrl(avatarUrl);
            profiles.save(profile);
            log.info("Updated profile with new avatar URL for user {}: {}", userId, avatarUrl);
        });

        return avatarUrl;
    }

    private ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getDisplayName(),
                p.getBio(),
                p.getAvatarUrl(),
                p.getVisibility().name()
        );
    }

    private Visibility parseVisibility(String value) {
        if (value == null) return Visibility.PUBLIC;
        try {
            return Visibility.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Visibility.PUBLIC;
        }
    }
}