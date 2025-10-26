package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    private final UserRepository users;
    private final SchoolRepository schools;

    UserRepositoryTest(@Autowired UserRepository users,
                       @Autowired SchoolRepository schools) {
        this.users = users;
        this.schools = schools;
    }

    @Test
    void existsByEmail_and_findByEmail_work() {
        var school = new School();
        school.setDomain("demo.edu");
        school.setName("Demo U");
        school = schools.save(school);

        var u = new User();
        u.setEmail("x@demo.edu");
        u.setPasswordHash("H");
        u.setSchool(school);
        users.save(u);

        assertThat(users.existsByEmail("x@demo.edu")).isTrue();
        assertThat(users.findByEmail("x@demo.edu")).isPresent();
        assertThat(users.findByEmail("y@demo.edu")).isEmpty();
    }

    @Test
    void uniqueEmail_violation() {
        var school = new School(); school.setDomain("demo2.edu"); school.setName("Demo2"); school = schools.saveAndFlush(school);

        var u1 = new User(); u1.setEmail("x@demo2.edu"); u1.setPasswordHash("H"); u1.setSchool(school); users.saveAndFlush(u1);

        var u2 = new User(); u2.setEmail("x@demo2.edu"); u2.setPasswordHash("H2"); u2.setSchool(school);

        assertThatThrownBy(() -> users.saveAndFlush(u2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void fkViolation_nonexistentSchool_throws() {
        var ghost = new School();
        ghost.setId(999L); // not persisted

        var u = new User();
        u.setEmail("z@demo.edu");
        u.setPasswordHash("H");
        u.setSchool(ghost);

        assertThatThrownBy(() -> users.saveAndFlush(u))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
