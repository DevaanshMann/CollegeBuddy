package com.collegebuddy.testutil;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.Connection;
import com.collegebuddy.domain.ConnectionRequest;
import com.collegebuddy.domain.ConnectionRequestStatus;
import com.collegebuddy.domain.Conversation;
import com.collegebuddy.domain.Message;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.Role;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;

public class TestDataFactory {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String DEFAULT_PASSWORD = "password123";

    public static User createActiveUser(String email, String campusDomain) {
        User u = new User();
        u.setEmail(email);
        u.setHashedPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        u.setCampusDomain(campusDomain);
        u.setStatus(AccountStatus.ACTIVE);
        u.setRole(Role.STUDENT);
        return u;
    }

    public static User createPendingUser(String email, String campusDomain) {
        User u = new User();
        u.setEmail(email);
        u.setHashedPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        u.setCampusDomain(campusDomain);
        u.setStatus(AccountStatus.PENDING_VERIFICATION);
        u.setRole(Role.STUDENT);
        return u;
    }

    public static User createActiveUser(String email, String campusDomain, String password) {
        User u = new User();
        u.setEmail(email);
        u.setHashedPassword(passwordEncoder.encode(password));
        u.setCampusDomain(campusDomain);
        u.setStatus(AccountStatus.ACTIVE);
        u.setRole(Role.STUDENT);
        return u;
    }

    public static Profile createProfile(Long userId, String displayName) {
        Profile p = new Profile();
        p.setUserId(userId);
        p.setDisplayName(displayName);
        p.setBio("Test bio for " + displayName);
        p.setVisibility(Visibility.PUBLIC);
        return p;
    }

    public static Profile createProfile(Long userId, String displayName, Visibility visibility) {
        Profile p = new Profile();
        p.setUserId(userId);
        p.setDisplayName(displayName);
        p.setBio("Test bio for " + displayName);
        p.setVisibility(visibility);
        return p;
    }

    public static Connection createConnection(Long userAId, Long userBId) {
        long a = Math.min(userAId, userBId);
        long b = Math.max(userAId, userBId);

        Connection conn = new Connection();
        conn.setUserAId(a);
        conn.setUserBId(b);
        conn.setCreatedAt(Instant.now());
        return conn;
    }

    public static ConnectionRequest createPendingRequest(Long fromUserId, Long toUserId, String message) {
        ConnectionRequest req = new ConnectionRequest();
        req.setFromUserId(fromUserId);
        req.setToUserId(toUserId);
        req.setMessage(message);
        req.setStatus(ConnectionRequestStatus.PENDING);
        req.setCreatedAt(Instant.now());
        return req;
    }

    public static Conversation createConversation(Long userAId, Long userBId) {
        long a = Math.min(userAId, userBId);
        long b = Math.max(userAId, userBId);

        Conversation conv = new Conversation();
        conv.setUserAId(a);
        conv.setUserBId(b);
        conv.setCreatedAt(Instant.now());
        return conv;
    }

    public static Message createMessage(Long conversationId, Long senderId, String body) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setBody(body);
        msg.setSentAt(Instant.now());
        return msg;
    }

    public static User createAdminUser(String email, String campusDomain) {
        User u = new User();
        u.setEmail(email);
        u.setHashedPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        u.setCampusDomain(campusDomain);
        u.setStatus(AccountStatus.ACTIVE);
        u.setRole(Role.ADMIN);
        return u;
    }

    public static String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    public static BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
