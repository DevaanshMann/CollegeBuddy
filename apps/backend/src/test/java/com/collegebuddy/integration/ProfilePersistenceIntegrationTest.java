package com.collegebuddy.integration;

import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.ProfileUpdateRequest;
import com.collegebuddy.testutil.BaseIntegrationTest;
import com.collegebuddy.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for profile persistence.
 * These tests validate the bug fix where profile data (displayName, bio)
 * was not persisting after navigation/page refresh.
 *
 * Root cause: GET /auth/me was not loading profile data from database.
 * Fix: AuthService.getUserById() now loads profile and includes in UserDto.
 */
@DisplayName("Profile Persistence Integration Tests")
class ProfilePersistenceIntegrationTest extends BaseIntegrationTest {

    private User user;
    private String token;

    @BeforeEach
    void setupUser() {
        user = TestDataFactory.createActiveUser("testuser@university.edu", "university.edu");
        user = userRepository.save(user);
        token = generateToken(user.getId(), user.getCampusDomain(), "STUDENT", user.getEmail(), "Initial Name");
    }

    @Test
    @DisplayName("should persist displayName and bio after profile update")
    void updateProfile_then_getAuthMe_shouldReturnSavedData() throws Exception {
        // Given: User updates their profile
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
                "My Custom Display Name",
                "This is my awesome bio!",
                null,
                "PUBLIC"
        );

        mockMvc.perform(put("/profile")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // When: User calls /auth/me (simulating page refresh or navigation)
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("My Custom Display Name"))
                .andExpect(jsonPath("$.email").value("testuser@university.edu"));

        // Verify profile was saved in database
        Profile profile = profileRepository.findById(user.getId()).orElseThrow();
        assert profile.getDisplayName().equals("My Custom Display Name");
        assert profile.getBio().equals("This is my awesome bio!");
    }

    @Test
    @DisplayName("should persist profile data across multiple navigation cycles")
    void updateProfile_multipleNavigations_shouldPersistData() throws Exception {
        // Given: User updates profile
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Persistent Name",
                "Persistent Bio",
                "https://example.com/avatar.jpg",
                "PUBLIC"
        );

        mockMvc.perform(put("/profile")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When: Simulate multiple page refreshes (calling /auth/me multiple times)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/auth/me")
                            .header("Authorization", bearerToken(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("Persistent Name"))
                    .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"));
        }

        // Then: Verify profile endpoint also returns correct data
        mockMvc.perform(get("/profile/" + user.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Persistent Name"))
                .andExpect(jsonPath("$.bio").value("Persistent Bio"));
    }

    @Test
    @DisplayName("should return profile data immediately after update via /auth/me")
    void updateProfile_immediateAuthMeCall_shouldReturnNewData() throws Exception {
        // Given: Profile with initial data
        Profile initialProfile = TestDataFactory.createProfile(user.getId(), "Initial Display");
        profileRepository.save(initialProfile);

        // When: Update profile
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Updated Display",
                "Updated Bio",
                null,
                "PRIVATE"
        );

        mockMvc.perform(put("/profile")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then: Immediately call /auth/me and verify new data is returned
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Display"))
                .andExpect(jsonPath("$.profileVisibility").value("PRIVATE"));
    }

    @Test
    @DisplayName("should return email as displayName when profile does not exist")
    void getAuthMe_noProfile_shouldReturnEmailAsDisplayName() throws Exception {
        // Given: User with no profile created yet

        // When: Call /auth/me
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("testuser@university.edu"))
                .andExpect(jsonPath("$.email").value("testuser@university.edu"));
    }

    @Test
    @DisplayName("should maintain profile consistency across auth/me and profile endpoints")
    void profileConsistency_authMeAndProfileEndpoint_shouldMatch() throws Exception {
        // Given: User with saved profile
        Profile profile = TestDataFactory.createProfile(user.getId(), "Consistent Name");
        profile.setBio("Consistent Bio");
        profile.setAvatarUrl("https://example.com/consistent.jpg");
        profileRepository.save(profile);

        // When: Fetch from both endpoints
        String authMeResponse = mockMvc.perform(get("/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String profileResponse = mockMvc.perform(get("/profile/" + user.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then: Both should have matching displayName, avatarUrl
        assert authMeResponse.contains("Consistent Name");
        assert authMeResponse.contains("https://example.com/consistent.jpg");
        assert profileResponse.contains("Consistent Name");
        assert profileResponse.contains("Consistent Bio");
    }

    @Test
    @DisplayName("should persist profile updates even when only displayName is changed")
    void updateProfile_onlyDisplayName_shouldPersist() throws Exception {
        // Given: Profile with existing data
        Profile profile = TestDataFactory.createProfile(user.getId(), "Old Name");
        profile.setBio("Original bio");
        profileRepository.save(profile);

        // When: Update only displayName
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "New Name",
                "Original bio", // same bio
                null,
                "PUBLIC"
        );

        mockMvc.perform(put("/profile")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then: Verify new displayName persists
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Name"));
    }

    @Test
    @DisplayName("should persist empty bio as empty string, not null")
    void updateProfile_emptyBio_shouldPersistAsEmptyString() throws Exception {
        // Given: Profile with bio
        Profile profile = TestDataFactory.createProfile(user.getId(), "User Name");
        profile.setBio("Some bio");
        profileRepository.save(profile);

        // When: Update with empty bio
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "User Name",
                "", // empty bio
                null,
                "PUBLIC"
        );

        mockMvc.perform(put("/profile")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value(""));

        // Then: Verify bio is empty (not null)
        Profile updated = profileRepository.findById(user.getId()).orElseThrow();
        assert updated.getBio().equals("");
    }
}
