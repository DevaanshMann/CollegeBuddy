package com.collegebuddy.testutil;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.User;

/**
 * Factory for creating sample entities during tests.
 */
public class TestDataFactory {

    public static User sampleActiveUser(Long id, String email, String campusDomain) {
        User u = new User();
        // setters because fields are private
        // (you'll add setters/getters or Lombok in real code)
        // e.g. u.setId(id);
        // e.g. u.setEmail(email);
        // e.g. u.setStatus(AccountStatus.ACTIVE);
        return u;
    }
}
