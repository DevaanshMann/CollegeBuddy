package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

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

    @Test
    void uniqueDomain_violation() {
        var s1 = new School(); s1.setDomain("dup.edu"); s1.setName("One"); schools.saveAndFlush(s1);

        var s2 = new School(); s2.setDomain("dup.edu"); s2.setName("Two");

        assertThatThrownBy(() -> schools.saveAndFlush(s2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
