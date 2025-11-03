package com.collegebuddy.profile;

import com.collegebuddy.dto.ProfileResponse;
import com.collegebuddy.dto.ProfileUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Handles creating/updating a user's profile (name, bio, avatar, visibility)
 */

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request){
//        TODO: save profile changes
        return ResponseEntity.ok(new ProfileResponse("name", "bio", "avatarURL", "PUBLIC"));
    }

    @GetMapping("/{UserId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId){
//        TODO: fetch profile, enforce visibility rules
        return ResponseEntity.ok(new ProfileResponse("name", "bio", "avatarURL", "PUBLIC"));
    }
}
