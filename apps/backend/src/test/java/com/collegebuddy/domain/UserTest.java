package com.collegebuddy.domain;

import com.collegebuddy.repo.SchoolRepository;
import com.collegebuddy.repo.UserRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // H2 in-memory
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"   // keep tests independent of migrations
})
class UserTest {

    @Autowired TestEntityManager em;
    @Autowired UserRepository users;
    @Autowired SchoolRepository schools;

    private School csun;

    @BeforeEach
    void seedSchool() {
        School s = new School();
        s.setDomain("csun.edu");
        s.setName("CSUN");
        csun = schools.saveAndFlush(s);
        assertThat(csun.getId()).isNotNull();
    }

    private User user(String email, String hash, School school, boolean verified) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setEmailVerified(verified);
        u.setSchool(school);
        return u;
    }

    @Test
    void insert_assignsId_and_relatesToSchool() {
        User saved = em.persistFlushFind(user("alice@csun.edu", "H1", csun, true));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("alice@csun.edu");
        assertThat(saved.getPasswordHash()).isEqualTo("H1");
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getSchool().getId()).isEqualTo(csun.getId());
    }

    @Test
    void update_roundTrip_persistsChanges() {
        User u = em.persistFlushFind(user("bob@csun.edu", "H2", csun, false));
        u.setPasswordHash("H2-new");
        u.setEmailVerified(true);

        User reloaded = em.persistFlushFind(u);
        assertThat(reloaded.getPasswordHash()).isEqualTo("H2-new");
        assertThat(reloaded.isEmailVerified()).isTrue();
    }

    @Test
    void notNull_email_violatesOnNull() {
        User u = user(null, "Hx", csun, true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            users.saveAndFlush(u); // triggers NOT NULL on email
        });
    }

    @Test
    void notNull_passwordHash_violatesOnNull() {
        User u = user("nohash@csun.edu", null, csun, true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            users.saveAndFlush(u); // triggers NOT NULL on password_hash
        });
    }

    @Test
    void notNull_school_violatesOnNull() {
        User u = user("noschool@csun.edu", "H", null, true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            users.saveAndFlush(u); // triggers NOT NULL on school_id FK
        });
    }

    @Test
    void unique_email_violatesOnDuplicate() {
        users.saveAndFlush(user("dup@csun.edu", "H1", csun, true));

        User duplicate = user("dup@csun.edu", "H2", csun, false);
        assertThrows(DataIntegrityViolationException.class, () -> {
            users.saveAndFlush(duplicate); // unique constraint on email
        });
    }

}
