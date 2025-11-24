package com.collegebuddy.integration;

import com.collegebuddy.domain.Connection;
import com.collegebuddy.domain.ConnectionRequest;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.RespondToConnectionDto;
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

@DisplayName("Connection Controller Integration Tests")
class ConnectionControllerIntegrationTest extends BaseIntegrationTest {

    private User user1;
    private User user2;
    private User user3; // Different campus
    private String token1;
    private String token2;

    @BeforeEach
    void setupUsers() {
        // Create users on same campus
        user1 = TestDataFactory.createActiveUser("user1@university.edu", "university.edu");
        user1 = userRepository.save(user1);
        Profile profile1 = TestDataFactory.createProfile(user1.getId(), "User One");
        profileRepository.save(profile1);

        user2 = TestDataFactory.createActiveUser("user2@university.edu", "university.edu");
        user2 = userRepository.save(user2);
        Profile profile2 = TestDataFactory.createProfile(user2.getId(), "User Two");
        profileRepository.save(profile2);

        // Create user on different campus
        user3 = TestDataFactory.createActiveUser("user3@othercampus.edu", "othercampus.edu");
        user3 = userRepository.save(user3);
        Profile profile3 = TestDataFactory.createProfile(user3.getId(), "User Three");
        profileRepository.save(profile3);

        // Generate tokens
        token1 = generateToken(user1.getId(), user1.getCampusDomain());
        token2 = generateToken(user2.getId(), user2.getCampusDomain());
    }

    @Nested
    @DisplayName("POST /connections/request")
    class SendConnectionRequestTests {

        @Test
        @DisplayName("should successfully send connection request to user on same campus")
        void sendRequest_sameCampus_shouldSucceed() throws Exception {
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    user2.getId(),
                    "Hi, let's connect!"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify request was created
            var requests = connectionRequestRepository.findAll();
            assert requests.size() == 1;
            assert requests.get(0).getFromUserId().equals(user1.getId());
            assert requests.get(0).getToUserId().equals(user2.getId());
        }

        @Test
        @DisplayName("should fail to send connection request to user on different campus")
        void sendRequest_differentCampus_shouldFail() throws Exception {
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    user3.getId(),
                    "Hi from another campus!"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should fail to send connection request to self")
        void sendRequest_toSelf_shouldFail() throws Exception {
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    user1.getId(),
                    "Talking to myself"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail if already connected")
        void sendRequest_alreadyConnected_shouldFail() throws Exception {
            // Create existing connection
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    user2.getId(),
                    "Connect again?"
            );

            mockMvc.perform(post("/connections/request")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail without authentication")
        void sendRequest_noAuth_shouldFail() throws Exception {
            SendConnectionRequestDto request = new SendConnectionRequestDto(
                    user2.getId(),
                    "No auth"
            );

            mockMvc.perform(post("/connections/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /connections/respond")
    class RespondToConnectionTests {

        @Test
        @DisplayName("should successfully accept connection request")
        void respond_accept_shouldCreateConnection() throws Exception {
            // Create pending request from user1 to user2
            ConnectionRequest req = TestDataFactory.createPendingRequest(user1.getId(), user2.getId(), "Please connect");
            req = connectionRequestRepository.save(req);

            RespondToConnectionDto response = new RespondToConnectionDto(req.getId(), "ACCEPT");

            mockMvc.perform(post("/connections/respond")
                            .header("Authorization", bearerToken(token2))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(response)))
                    .andExpect(status().isOk());

            // Verify connection was created
            var connections = connectionRepository.findAll();
            assert connections.size() == 1;
        }

        @Test
        @DisplayName("should successfully decline connection request")
        void respond_decline_shouldNotCreateConnection() throws Exception {
            ConnectionRequest req = TestDataFactory.createPendingRequest(user1.getId(), user2.getId(), "Please connect");
            req = connectionRequestRepository.save(req);

            RespondToConnectionDto response = new RespondToConnectionDto(req.getId(), "DECLINE");

            mockMvc.perform(post("/connections/respond")
                            .header("Authorization", bearerToken(token2))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(response)))
                    .andExpect(status().isOk());

            // Verify no connection was created
            var connections = connectionRepository.findAll();
            assert connections.isEmpty();
        }

        @Test
        @DisplayName("should fail if not the recipient of the request")
        void respond_notRecipient_shouldFail() throws Exception {
            ConnectionRequest req = TestDataFactory.createPendingRequest(user1.getId(), user2.getId(), "Please connect");
            req = connectionRequestRepository.save(req);

            // user1 tries to respond to their own request
            RespondToConnectionDto response = new RespondToConnectionDto(req.getId(), "ACCEPT");

            mockMvc.perform(post("/connections/respond")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(response)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /connections")
    class ListConnectionsTests {

        @Test
        @DisplayName("should return connections and pending requests")
        void listConnections_shouldReturnAll() throws Exception {
            // Create a connection
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            mockMvc.perform(get("/connections")
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.connections").isArray())
                    .andExpect(jsonPath("$.connections", hasSize(1)));
        }

        @Test
        @DisplayName("should return empty list when no connections")
        void listConnections_noConnections_shouldReturnEmpty() throws Exception {
            mockMvc.perform(get("/connections")
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.connections").isArray())
                    .andExpect(jsonPath("$.connections", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /connections/{userId}")
    class DisconnectTests {

        @Test
        @DisplayName("should successfully disconnect from connected user")
        void disconnect_connected_shouldSucceed() throws Exception {
            // Create connection
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            mockMvc.perform(delete("/connections/" + user2.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk());

            // Verify connection was deleted
            var connections = connectionRepository.findAll();
            assert connections.isEmpty();
        }

        @Test
        @DisplayName("should fail to disconnect from non-connected user")
        void disconnect_notConnected_shouldFail() throws Exception {
            mockMvc.perform(delete("/connections/" + user2.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should fail to disconnect from self")
        void disconnect_self_shouldFail() throws Exception {
            mockMvc.perform(delete("/connections/" + user1.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().is4xxClientError());
        }
    }
}
