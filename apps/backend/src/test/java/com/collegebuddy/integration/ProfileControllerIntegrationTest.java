package com.collegebuddy.integration;

import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.ProfileUpdateRequest;
import com.collegebuddy.testutil.BaseIntegrationTest;
import com.collegebuddy.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Profile Controller Integration Tests")
class ProfileControllerIntegrationTest extends BaseIntegrationTest {

    private User user1;
    private User user2;
    private User privateUser;
    private String token1;
    private String token2;

    @BeforeEach
    void setupUsers() {
        // Create users
        user1 = TestDataFactory.createActiveUser("profile1@university.edu", "university.edu");
        user1 = userRepository.save(user1);
        Profile profile1 = TestDataFactory.createProfile(user1.getId(), "User One");
        profileRepository.save(profile1);

        user2 = TestDataFactory.createActiveUser("profile2@university.edu", "university.edu");
        user2 = userRepository.save(user2);
        Profile profile2 = TestDataFactory.createProfile(user2.getId(), "User Two");
        profileRepository.save(profile2);

        privateUser = TestDataFactory.createActiveUser("private@university.edu", "university.edu");
        privateUser = userRepository.save(privateUser);
        Profile privateProfile = TestDataFactory.createProfile(privateUser.getId(), "Private User", Visibility.PRIVATE);
        profileRepository.save(privateProfile);

        // Generate tokens
        token1 = generateToken(user1.getId(), user1.getCampusDomain());
        token2 = generateToken(user2.getId(), user2.getCampusDomain());
    }

    @Nested
    @DisplayName("PUT /profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("should successfully update own profile")
        void updateProfile_ownProfile_shouldSucceed() throws Exception {
            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "New Display Name",
                    "Updated bio content",
                    "https://example.com/avatar.jpg",
                    "PUBLIC"
            );

            mockMvc.perform(put("/profile")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("New Display Name"))
                    .andExpect(jsonPath("$.bio").value("Updated bio content"));

            // Verify profile was updated in database
            var profile = profileRepository.findById(user1.getId()).orElseThrow();
            assert profile.getDisplayName().equals("New Display Name");
            assert profile.getBio().equals("Updated bio content");
        }

        @Test
        @DisplayName("should update profile visibility to private")
        void updateProfile_setPrivate_shouldSucceed() throws Exception {
            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "User One",
                    "Private now",
                    null,
                    "PRIVATE"
            );

            mockMvc.perform(put("/profile")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.visibility").value("PRIVATE"));
        }

        @Test
        @DisplayName("should fail without authentication")
        void updateProfile_noAuth_shouldFail() throws Exception {
            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "Unauthorized",
                    "Bio",
                    null,
                    "PUBLIC"
            );

            mockMvc.perform(put("/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /profile/{userId}")
    class GetProfileTests {

        @Test
        @DisplayName("should successfully get own profile")
        void getProfile_ownProfile_shouldSucceed() throws Exception {
            mockMvc.perform(get("/profile/" + user1.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("User One"));
        }

        @Test
        @DisplayName("should successfully get public profile of another user")
        void getProfile_publicProfile_shouldSucceed() throws Exception {
            mockMvc.perform(get("/profile/" + user2.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("User Two"));
        }

        @Test
        @DisplayName("should restrict access to private profile of non-connected user")
        void getProfile_privateProfile_notConnected_shouldRestrict() throws Exception {
            mockMvc.perform(get("/profile/" + privateUser.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should fail without authentication")
        void getProfile_noAuth_shouldFail() throws Exception {
            mockMvc.perform(get("/profile/" + user1.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should fail for non-existent user")
        void getProfile_nonExistent_shouldFail() throws Exception {
            // API returns 403 (forbidden/not found) for non-existent profiles
            mockMvc.perform(get("/profile/99999")
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isForbidden());
        }
    }
}
