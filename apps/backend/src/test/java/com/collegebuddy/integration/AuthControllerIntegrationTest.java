package com.collegebuddy.integration;

import com.collegebuddy.domain.User;
import com.collegebuddy.dto.LoginRequest;
import com.collegebuddy.dto.SignupRequest;
import com.collegebuddy.testutil.BaseIntegrationTest;
import com.collegebuddy.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /auth/signup")
    class SignupTests {

        @Test
        @DisplayName("should successfully create a new user with valid .edu email")
        void signup_withValidEduEmail_shouldSucceed() throws Exception {
            SignupRequest request = new SignupRequest(
                    "newuser@university.edu",
                    "SecurePass123!",
                    "university.edu"
            );

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());

            // Verify user was created in database
            var user = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals("newuser@university.edu"))
                    .findFirst();
            assert user.isPresent();
        }

        @Test
        @DisplayName("should fail signup with duplicate email")
        void signup_withDuplicateEmail_shouldFail() throws Exception {
            // Create existing user
            User existingUser = TestDataFactory.createActiveUser("existing@university.edu", "university.edu");
            userRepository.save(existingUser);

            SignupRequest request = new SignupRequest(
                    "existing@university.edu",
                    "SecurePass123!",
                    "university.edu"
            );

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail signup with non-.edu email")
        void signup_withNonEduEmail_shouldFail() throws Exception {
            SignupRequest request = new SignupRequest(
                    "user@gmail.com",
                    "SecurePass123!",
                    "gmail.com"
            );

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("should successfully login with valid credentials")
        void login_withValidCredentials_shouldReturnJwt() throws Exception {
            // Create and save an active user
            User user = TestDataFactory.createActiveUser("loginuser@university.edu", "university.edu");
            userRepository.save(user);

            LoginRequest request = new LoginRequest(
                    "loginuser@university.edu",
                    TestDataFactory.getDefaultPassword()
            );

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jwt").exists())
                    .andExpect(jsonPath("$.jwt").isNotEmpty());
        }

        @Test
        @DisplayName("should fail login with wrong password")
        void login_withWrongPassword_shouldFail() throws Exception {
            User user = TestDataFactory.createActiveUser("wrongpass@university.edu", "university.edu");
            userRepository.save(user);

            LoginRequest request = new LoginRequest(
                    "wrongpass@university.edu",
                    "wrongpassword"
            );

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should fail login with non-existent email")
        void login_withNonExistentEmail_shouldFail() throws Exception {
            LoginRequest request = new LoginRequest(
                    "nonexistent@university.edu",
                    "anypassword"
            );

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should fail login with pending verification status")
        void login_withPendingVerification_shouldFail() throws Exception {
            User user = TestDataFactory.createPendingUser("pending@university.edu", "university.edu");
            userRepository.save(user);

            LoginRequest request = new LoginRequest(
                    "pending@university.edu",
                    TestDataFactory.getDefaultPassword()
            );

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }
}
