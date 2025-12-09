package com.collegebuddy.testutil;

import com.collegebuddy.repo.ConnectionRepository;
import com.collegebuddy.repo.ConnectionRequestRepository;
import com.collegebuddy.repo.ConversationRepository;
import com.collegebuddy.repo.MessageRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProfileRepository profileRepository;

    @Autowired
    protected ConnectionRepository connectionRepository;

    @Autowired
    protected ConnectionRequestRepository connectionRequestRepository;

    @Autowired
    protected ConversationRepository conversationRepository;

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired(required = false)
    protected com.collegebuddy.repo.BlockedUserRepository blockedUserRepository;

    @Autowired(required = false)
    protected com.collegebuddy.repo.VerificationTokenRepository verificationTokenRepository;

    @Autowired(required = false)
    protected com.collegebuddy.repo.PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void cleanDatabase() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        connectionRepository.deleteAll();
        connectionRequestRepository.deleteAll();

        if (blockedUserRepository != null) {
            blockedUserRepository.deleteAll();
        }
        if (verificationTokenRepository != null) {
            verificationTokenRepository.deleteAll();
        }
        if (passwordResetTokenRepository != null) {
            passwordResetTokenRepository.deleteAll();
        }

        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String generateToken(Long userId, String campusDomain) {
        return jwtService.issueToken(userId, campusDomain, "STUDENT", "test@" + campusDomain, "Test User");
    }

    protected String generateToken(Long userId, String campusDomain, String role, String email, String displayName) {
        return jwtService.issueToken(userId, campusDomain, role, email, displayName);
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}
