package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

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
}
