package com.collegebuddy.integration;

import com.collegebuddy.domain.BlockedUser;
import com.collegebuddy.domain.Connection;
import com.collegebuddy.domain.ConnectionRequest;
import com.collegebuddy.domain.ConnectionRequestStatus;
import com.collegebuddy.domain.Conversation;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.BlockUserRequest;
import com.collegebuddy.dto.SendConnectionRequestDto;
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

/**
 * Integration tests for user blocking functionality.
 * Tests block, unblock, list blocked users, and blocking effects on connections/search.
 */
@DisplayName("Blocking Controller Integration Tests")
class BlockingControllerIntegrationTest extends BaseIntegrationTest {

    private User alice;
    private User bob;
    private User charlie;
    private String aliceToken;
    private String bobToken;
    private String charlieToken;

    @BeforeEach
    void setupUsers() {
        alice = TestDataFactory.createActiveUser("alice@university.edu", "university.edu");
        alice = userRepository.save(alice);
        Profile aliceProfile = TestDataFactory.createProfile(alice.getId(), "Alice");
        profileRepository.save(aliceProfile);

        bob = TestDataFactory.createActiveUser("bob@university.edu", "university.edu");
        bob = userRepository.save(bob);
        Profile bobProfile = TestDataFactory.createProfile(bob.getId(), "Bob");
        profileRepository.save(bobProfile);

        charlie = TestDataFactory.createActiveUser("charlie@university.edu", "university.edu");
        charlie = userRepository.save(charlie);
        Profile charlieProfile = TestDataFactory.createProfile(charlie.getId(), "Charlie");
        profileRepository.save(charlieProfile);

        aliceToken = generateToken(alice.getId(), alice.getCampusDomain());
        bobToken = generateToken(bob.getId(), bob.getCampusDomain());
        charlieToken = generateToken(charlie.getId(), charlie.getCampusDomain());
    }

    @Nested
    @DisplayName("POST /blocked-users")
    class BlockUserTests {

        @Test
        @DisplayName("should successfully block a user")
        void blockUser_validUser_shouldSucceed() throws Exception {
            // When: Alice blocks Bob
            BlockUserRequest request = new BlockUserRequest(bob.getId());

            mockMvc.perform(post("/blocked-users")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Then: Verify block exists in database
            if (blockedUserRepository != null) {
                boolean blocked = blockedUserRepository.existsByBlockerIdAndBlockedId(alice.getId(), bob.getId());
                assert blocked;
            }
        }

        @Test
        @DisplayName("should remove existing connection when blocking")
        void blockUser_existingConnection_shouldRemoveConnection() throws Exception {
            // Given: Alice and Bob are connected
            Connection connection = TestDataFactory.createConnection(alice.getId(), bob.getId());
            connectionRepository.save(connection);

            // When: Alice blocks Bob
            BlockUserRequest request = new BlockUserRequest(bob.getId());

            mockMvc.perform(post("/blocked-users")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Then: Connection should be removed
            var connections = connectionRepository.findAll();
            assert connections.isEmpty() : "Connection should be removed after blocking";
        }

        @Test
        @DisplayName("should fail to block self")
        void blockUser_self_shouldFail() throws Exception {
            BlockUserRequest request = new BlockUserRequest(alice.getId());

            mockMvc.perform(post("/blocked-users")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail without authentication")
        void blockUser_noAuth_shouldFail() throws Exception {
            BlockUserRequest request = new BlockUserRequest(bob.getId());

            mockMvc.perform(post("/blocked-users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /blocked-users/{userId}")
    class UnblockUserTests {

        @Test
        @DisplayName("should successfully unblock a user")
        void unblockUser_blockedUser_shouldSucceed() throws Exception {
            // Given: Alice has blocked Bob
            if (blockedUserRepository != null) {
                BlockedUser block = new BlockedUser(alice.getId(), bob.getId());
                blockedUserRepository.save(block);
            }

            // When: Alice unblocks Bob
            mockMvc.perform(delete("/blocked-users/" + bob.getId())
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk());

            // Then: Verify block is removed
            if (blockedUserRepository != null) {
                boolean blocked = blockedUserRepository.existsByBlockerIdAndBlockedId(alice.getId(), bob.getId());
                assert !blocked : "User should be unblocked";
            }
        }

        @Test
        @DisplayName("unblock should fail if user was not blocked")
        void unblockUser_notBlocked_shouldFail() throws Exception {
            // When: Alice tries to unblock Bob (who was never blocked)
            mockMvc.perform(delete("/blocked-users/" + bob.getId())
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should fail without authentication")
        void unblockUser_noAuth_shouldFail() throws Exception {
            mockMvc.perform(delete("/blocked-users/" + bob.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /blocked-users")
    class GetBlockedUsersTests {

        @Test
        @DisplayName("should return list of blocked users")
        void getBlockedUsers_multipleBlocked_shouldReturnAll() throws Exception {
            // Given: Alice has blocked Bob and Charlie
            if (blockedUserRepository != null) {
                BlockedUser block1 = new BlockedUser(alice.getId(), bob.getId());
                blockedUserRepository.save(block1);

                BlockedUser block2 = new BlockedUser(alice.getId(), charlie.getId());
                blockedUserRepository.save(block2);
            }

            // When: Alice gets blocked users list
            mockMvc.perform(get("/blocked-users")
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("should return empty list when no users blocked")
        void getBlockedUsers_noneBlocked_shouldReturnEmpty() throws Exception {
            mockMvc.perform(get("/blocked-users")
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should fail without authentication")
        void getBlockedUsers_noAuth_shouldFail() throws Exception {
            mockMvc.perform(get("/blocked-users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /blocked-users/check/{userId}")
    class CheckBlockedTests {

        @Test
        @DisplayName("should return true when user is blocked")
        void isBlocked_blockedUser_shouldReturnTrue() throws Exception {
            // Given: Alice has blocked Bob
            if (blockedUserRepository != null) {
                BlockedUser block = new BlockedUser(alice.getId(), bob.getId());
                blockedUserRepository.save(block);
            }

            // When: Check if Bob is blocked
            mockMvc.perform(get("/blocked-users/check/" + bob.getId())
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));
        }

        @Test
        @DisplayName("should return false when user is not blocked")
        void isBlocked_notBlockedUser_shouldReturnFalse() throws Exception {
            mockMvc.perform(get("/blocked-users/check/" + bob.getId())
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(false));
        }
    }

    @Nested
    @DisplayName("Blocking Effects")
    class BlockingEffectsTests {

        @Test
        @DisplayName("cannot send connection request to blocked user")
        void blockUser_thenSendRequest_shouldFail() throws Exception {
            // Given: Alice has blocked Bob
            if (blockedUserRepository != null) {
                BlockedUser block = new BlockedUser(alice.getId(), bob.getId());
                blockedUserRepository.save(block);
            }

            // When: Alice tries to send connection request to Bob
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    bob.getId(),
                    "Let's connect"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("blocked user cannot send connection request to blocker")
        void blockedUser_cannotSendRequestToBlocker() throws Exception {
            // Given: Alice has blocked Bob
            if (blockedUserRepository != null) {
                BlockedUser block = new BlockedUser(alice.getId(), bob.getId());
                blockedUserRepository.save(block);
            }

            // When: Bob tries to send connection request to Alice
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    alice.getId(),
                    "Let's connect"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(bobToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("after unblock, users must send fresh connection request")
        void unblock_thenConnect_requiresFreshRequest() throws Exception {
            // Given: Alice blocked Bob, they were previously connected
            if (blockedUserRepository != null) {
                BlockedUser block = new BlockedUser(alice.getId(), bob.getId());
                blockedUserRepository.save(block);
            }

            // When: Alice unblocks Bob
            mockMvc.perform(delete("/blocked-users/" + bob.getId())
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk());

            // Then: They should not be automatically connected
            var connections = connectionRepository.findAll();
            assert connections.isEmpty() : "No connection should exist after unblock";

            // And: Bob can send a fresh connection request
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    alice.getId(),
                    "Let's connect again"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(bobToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify request was created
            var requests = connectionRequestRepository.findAll();
            assert !requests.isEmpty() : "Connection request should be created";
        }
    }
}
