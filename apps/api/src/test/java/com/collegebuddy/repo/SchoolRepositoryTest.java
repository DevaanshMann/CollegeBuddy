package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SchoolRepositoryTest {

    private final SchoolRepository schools;

    SchoolRepositoryTest(@Autowired SchoolRepository schools) {
        this.schools = schools;
    }

    @Test
    void findByDomain_returnsSavedSchool() {
        var s = new School();
        s.setDomain("abc.edu");
        s.setName("ABC University");
        schools.save(s);

        assertThat(schools.findByDomain("abc.edu")).isPresent();
        assertThat(schools.findByDomain("none.edu")).isEmpty();
    }
}
