package com.collegebuddy.profile;

import com.collegebuddy.common.exceptions.ProfileVisibilityException;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.ProfileResponse;
import com.collegebuddy.dto.ProfileUpdateRequest;
import com.collegebuddy.repo.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profiles;

    public ProfileService(ProfileRepository profiles) {
        this.profiles = profiles;
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
            // no profile yet – you can choose to return minimal info later
            throw new ProfileVisibilityException("Profile not found");
        }

        Profile profile = opt.get();

        if (profile.getVisibility() == Visibility.PRIVATE && !targetUserId.equals(requesterUserId)) {
            throw new ProfileVisibilityException("Profile is private");
        }

        return toResponse(profile);
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
