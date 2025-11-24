package com.collegebuddy.integration;

import com.collegebuddy.domain.*;
import com.collegebuddy.dto.SendMessageRequest;
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

@DisplayName("Messaging Controller Integration Tests")
class MessagingControllerIntegrationTest extends BaseIntegrationTest {

    private User user1;
    private User user2;
    private User user3; // Not connected to user1
    private String token1;
    private String token2;

    @BeforeEach
    void setupUsers() {
        // Create users
        user1 = TestDataFactory.createActiveUser("sender@university.edu", "university.edu");
        user1 = userRepository.save(user1);
        profileRepository.save(TestDataFactory.createProfile(user1.getId(), "Sender"));

        user2 = TestDataFactory.createActiveUser("receiver@university.edu", "university.edu");
        user2 = userRepository.save(user2);
        profileRepository.save(TestDataFactory.createProfile(user2.getId(), "Receiver"));

        user3 = TestDataFactory.createActiveUser("stranger@university.edu", "university.edu");
        user3 = userRepository.save(user3);
        profileRepository.save(TestDataFactory.createProfile(user3.getId(), "Stranger"));

        // Generate tokens
        token1 = generateToken(user1.getId(), user1.getCampusDomain());
        token2 = generateToken(user2.getId(), user2.getCampusDomain());
    }

    @Nested
    @DisplayName("POST /messages/send")
    class SendMessageTests {

        @Test
        @DisplayName("should successfully send message to connected user")
        void sendMessage_connected_shouldSucceed() throws Exception {
            // Create connection between user1 and user2
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            // Pre-create conversation (H2 doesn't support PostgreSQL's ON CONFLICT)
            Conversation conv = TestDataFactory.createConversation(user1.getId(), user2.getId());
            conversationRepository.save(conv);

            SendMessageRequest request = new SendMessageRequest(
                    user2.getId(),
                    "Hello, how are you?"
            );

            mockMvc.perform(post("/messages/send")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body").value("Hello, how are you?"))
                    .andExpect(jsonPath("$.senderId").value(user1.getId()));

            // Verify message was saved
            var messages = messageRepository.findAll();
            assert messages.size() == 1;
            assert messages.get(0).getBody().equals("Hello, how are you?");
        }

        @Test
        @DisplayName("should fail to send message to non-connected user")
        void sendMessage_notConnected_shouldFail() throws Exception {
            SendMessageRequest request = new SendMessageRequest(
                    user3.getId(),
                    "Hi stranger!"
            );

            mockMvc.perform(post("/messages/send")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should fail without authentication")
        void sendMessage_noAuth_shouldFail() throws Exception {
            SendMessageRequest request = new SendMessageRequest(
                    user2.getId(),
                    "No auth message"
            );

            mockMvc.perform(post("/messages/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should send message when conversation already exists")
        void sendMessage_existingConversation_shouldSucceed() throws Exception {
            // Create connection
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            // Pre-create conversation
            Conversation conv = TestDataFactory.createConversation(user1.getId(), user2.getId());
            conversationRepository.save(conv);

            SendMessageRequest request = new SendMessageRequest(
                    user2.getId(),
                    "Another message!"
            );

            mockMvc.perform(post("/messages/send")
                            .header("Authorization", bearerToken(token1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body").value("Another message!"));

            // Verify message was added to existing conversation
            var messages = messageRepository.findAll();
            assert messages.size() == 1;
        }
    }

    @Nested
    @DisplayName("GET /messages/conversation/{otherUserId}")
    class GetConversationTests {

        @Test
        @DisplayName("should return conversation with messages")
        void getConversation_withMessages_shouldReturnAll() throws Exception {
            // Create connection and conversation
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            Conversation conv = TestDataFactory.createConversation(user1.getId(), user2.getId());
            conv = conversationRepository.save(conv);

            // Create some messages
            Message msg1 = TestDataFactory.createMessage(conv.getId(), user1.getId(), "Hey!");
            Message msg2 = TestDataFactory.createMessage(conv.getId(), user2.getId(), "Hi there!");
            messageRepository.save(msg1);
            messageRepository.save(msg2);

            mockMvc.perform(get("/messages/conversation/" + user2.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messages").isArray())
                    .andExpect(jsonPath("$.messages", hasSize(2)));
        }

        @Test
        @DisplayName("should return empty conversation for connected users with no messages")
        void getConversation_noMessages_shouldReturnEmpty() throws Exception {
            // Create connection
            Connection conn = TestDataFactory.createConnection(user1.getId(), user2.getId());
            connectionRepository.save(conn);

            // Pre-create conversation (H2 doesn't support PostgreSQL's ON CONFLICT)
            Conversation conv = TestDataFactory.createConversation(user1.getId(), user2.getId());
            conversationRepository.save(conv);

            mockMvc.perform(get("/messages/conversation/" + user2.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messages").isArray())
                    .andExpect(jsonPath("$.messages", hasSize(0)));
        }

        @Test
        @DisplayName("should fail to get conversation with non-connected user")
        void getConversation_notConnected_shouldFail() throws Exception {
            mockMvc.perform(get("/messages/conversation/" + user3.getId())
                            .header("Authorization", bearerToken(token1)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should fail without authentication")
        void getConversation_noAuth_shouldFail() throws Exception {
            mockMvc.perform(get("/messages/conversation/" + user2.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
