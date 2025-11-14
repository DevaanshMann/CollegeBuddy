package com.collegebuddy.profile;

import com.collegebuddy.common.exceptions.ProfileVisibilityException;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.ProfileResponse;
import com.collegebuddy.dto.ProfileUpdateRequest;
import com.collegebuddy.repo.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profiles;

    // Directory to store uploaded images (relative to project root)
    private static final String UPLOAD_DIR = "uploads/avatars/";

    public ProfileService(ProfileRepository profiles) {
        this.profiles = profiles;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
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
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String filename = userId + "_" + UUID.randomUUID() + extension;
            Path targetPath = Paths.get(UPLOAD_DIR + filename);

            // Save file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return ONLY the relative path (not full URL)
            String avatarUrl = "/uploads/avatars/" + filename;

            // Update user's profile with new avatar URL
            profiles.findById(userId).ifPresent(profile -> {
                // Delete old avatar file if it exists and is local
                if (profile.getAvatarUrl() != null && profile.getAvatarUrl().contains("/uploads/")) {
                    try {
                        String oldPath = profile.getAvatarUrl();
                        if (oldPath.contains("/uploads/avatars/")) {
                            // Extract filename - handle both full URLs and relative paths
                            String oldFilename = oldPath.substring(oldPath.lastIndexOf("/") + 1);
                            // Remove any query parameters if present
                            if (oldFilename.contains("?")) {
                                oldFilename = oldFilename.substring(0, oldFilename.indexOf("?"));
                            }
                            Files.deleteIfExists(Paths.get(UPLOAD_DIR + oldFilename));
                        }
                    } catch (IOException e) {
                        System.err.println("Could not delete old avatar: " + e.getMessage());
                    }
                }

                // Save ONLY the relative path
                profile.setAvatarUrl(avatarUrl);
                profiles.save(profile);
                System.out.println("Saved profile with avatar URL: " + avatarUrl); // Debug
            });

            System.out.println("Avatar uploaded successfully: " + avatarUrl); // Debug
            return avatarUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
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