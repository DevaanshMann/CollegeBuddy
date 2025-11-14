package com.collegebuddy.profile;

import com.collegebuddy.dto.ProfileResponse;
import com.collegebuddy.dto.ProfileUpdateRequest;
import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        ProfileResponse resp = profileService.updateProfile(current.id(), request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        ProfileResponse resp = profileService.getProfile(userId, current.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        String avatarUrl = profileService.uploadAvatar(current.id(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }
}