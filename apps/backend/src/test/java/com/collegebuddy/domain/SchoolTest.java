package com.collegebuddy.domain;

import com.collegebuddy.repo.SchoolRepository;
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
        "spring.flyway.enabled=false"
})
public class SchoolTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SchoolRepository repo;

    private static School school(String domain, String name) {
        School s = new School();
        s.setDomain(domain);
        s.setName(name);
        return s;
    }

    @Test
    void insert_assignsId_and_canBeReadBack() {
        School s = school("csun.edu", "California State University, Northridge");
        School saved = em.persistFlushFind(s);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDomain()).isEqualTo("csun.edu");
        assertThat(saved.getName()).contains("Northridge");
    }

    @Test
    void update_roundTrip_persistsChanges() {
        School s = em.persistFlushFind(school("cpp.edu", "Cal Poly Pomona"));
        s.setName("California State Polytechnic University, Pomona");

        School merged = em.persistFlushFind(s);
        assertThat(merged.getName()).contains("Polytechnic");
        assertThat(merged.getDomain()).isEqualTo("cpp.edu");
    }

    // Use the Spring Data repository for constraint tests so we get Spring’s translated exception:
    @Test
    void notNull_domain_violatesOnNull() {
        School s = school(null, "Some Name");

        assertThrows(DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(s); // triggers NOT NULL constraint via Spring Data -> translated
        });
    }

    @Test
    void notNull_name_violatesOnNull() {
        School s = school("sdsu.edu", null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(s);
        });
    }

    @Test
    void unique_domain_violatesOnDuplicate() {
        repo.saveAndFlush(school("uci.edu", "UC Irvine"));

        School dup = school("uci.edu", "UCI Duplicate Name OK But Domain Unique");
        assertThrows(DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(dup); // unique constraint on domain
        });
    }
}
