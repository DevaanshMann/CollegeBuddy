//package com.collegebuddy.repo;
//
//import com.collegebuddy.domain.School;
//import com.collegebuddy.domain.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@ActiveProfiles("test")
//class UserRepositoryTest {
//
//    private final UserRepository users;
//    private final SchoolRepository schools;
//
//    UserRepositoryTest(@Autowired UserRepository users,
//                       @Autowired SchoolRepository schools) {
//        this.users = users;
//        this.schools = schools;
//    }
//
//    @Test
//    void existsByEmail_and_findByEmail_work() {
//        var school = new School();
//        school.setDomain("demo.edu");
//        school.setName("Demo U");
//        school = schools.save(school);
//
//        var u = new User();
//        u.setEmail("x@demo.edu");
//        u.setPasswordHash("H");
//        u.setSchool(school);
//        users.save(u);
//
//        assertThat(users.existsByEmail("x@demo.edu")).isTrue();
//        assertThat(users.findByEmail("x@demo.edu")).isPresent();
//        assertThat(users.findByEmail("y@demo.edu")).isEmpty();
//    }
//}


package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // H2 in-memory
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"   // avoid Flyway interference in unit tests
})
class UserRepositoryTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private TestEntityManager em;

    private School school;

    @BeforeEach
    void seedSchool() {
        school = new School();
        school.setName("Cal Poly Pomona");
        school.setDomain("cpp.edu");
        em.persistAndFlush(school);
    }

    private User newUser(String email, String hash) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setSchool(school);
        u.setEmailVerified(true);
        return u;
    }

    // ---------------- existsByEmail ----------------

    @Test
    void existsByEmail_trueAfterPersist() {
        em.persistAndFlush(newUser("alice@cpp.edu", "H1"));
        assertThat(users.existsByEmail("alice@cpp.edu")).isTrue();
    }

    @Test
    void existsByEmail_falseWhenAbsent() {
        assertThat(users.existsByEmail("nobody@cpp.edu")).isFalse();
    }

    // ADDED #1 for existsByEmail: mixed-case should not match
    @Test
    void existsByEmail_mixedCaseQuery_doesNotMatchStoredLowercase() {
        em.persistAndFlush(newUser("bob@cpp.edu", "Hb"));
        assertThat(users.existsByEmail("BOB@CPP.EDU")).isFalse();
    }

    // ADDED #2 for existsByEmail: whitespace should not match
    @Test
    void existsByEmail_withLeadingTrailingSpaces_returnsFalse() {
        em.persistAndFlush(newUser("cara@cpp.edu", "Hc"));
        assertThat(users.existsByEmail("  cara@cpp.edu ")).isFalse();
    }

    // ---------------- findByEmail ----------------

    @Test
    void findByEmail_returnsUserWhenPresent() {
        User saved = em.persistFlushFind(newUser("bob@cpp.edu", "H2"));

        Optional<User> found = users.findByEmail("bob@cpp.edu");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getEmail()).isEqualTo("bob@cpp.edu");
        assertThat(found.get().getPasswordHash()).isEqualTo("H2");
        assertThat(found.get().getSchool().getId()).isEqualTo(school.getId());
        assertThat(found.get().isEmailVerified()).isTrue();
    }

    @Test
    void findByEmail_returnsEmptyWhenAbsent() {
        assertThat(users.findByEmail("ghost@cpp.edu")).isEmpty();
    }

    // ADDED #1 for findByEmail: returns the correct user among many
    @Test
    void findByEmail_returnsCorrectUserAmongMany() {
        em.persist(newUser("u1@cpp.edu", "H1"));
        em.persist(newUser("target@cpp.edu", "HT"));
        em.persist(newUser("u3@cpp.edu", "H3"));
        em.flush();

        Optional<User> found = users.findByEmail("target@cpp.edu");
        assertThat(found).isPresent();
        assertThat(found.get().getPasswordHash()).isEqualTo("HT");
        assertThat(found.get().getEmail()).isEqualTo("target@cpp.edu");
    }

    // ADDED #2 for findByEmail: after updating email, old not found; new found
    @Test
    void findByEmail_afterEmailUpdate_oldEmailNotFound_newEmailFound() {
        User u = em.persistFlushFind(newUser("old@cpp.edu", "H0"));
        u.setEmail("new@cpp.edu");
        users.saveAndFlush(u);

        assertThat(users.findByEmail("old@cpp.edu")).isEmpty();
        Optional<User> found = users.findByEmail("new@cpp.edu");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(u.getId());
    }

    // ---------------- save & update round-trip ----------------

    @Test
    void save_update_roundTrip() {
        User u = em.persistFlushFind(newUser("carol@cpp.edu", "H3"));
        u.setPasswordHash("H3-new");
        users.saveAndFlush(u);

        User reloaded = em.find(User.class, u.getId());
        assertThat(reloaded.getPasswordHash()).isEqualTo("H3-new");
    }

    // ---------------- paging/sorting helpers ----------------

    @Test
    void pagingAndSorting_byEmail() {
        em.persist(newUser("zeta@cpp.edu", "Hz"));
        em.persist(newUser("alpha@cpp.edu", "Ha"));
        em.persist(newUser("mid@cpp.edu", "Hm"));
        em.flush();

        Page<User> page0 = users.findAll(PageRequest.of(0, 2, Sort.by("email").ascending()));
        assertThat(page0.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page0.getContent()).extracting(User::getEmail)
                .startsWith("alpha@cpp.edu");

        Page<User> page1 = users.findAll(PageRequest.of(1, 2, Sort.by("email").ascending()));
        assertThat(page1.getContent()).isNotEmpty();
    }

    // ---------------- exact-match behavior ----------------

    @Test
    void findByEmail_isExactMatch_notTrimmedOrCaseFolded() {
        em.persistAndFlush(newUser("casey@cpp.edu", "Hc"));

        // Leading/trailing whitespace won't match unless normalized
        assertThat(users.findByEmail(" casey@cpp.edu ")).isEmpty();

        // Different case won't match in most H2/Hibernate defaults (depends on collation)
        assertThat(users.findByEmail("CASEY@CPP.EDU")).isEmpty();
    }

    // Optional: enable only if "email" is unique in schema (e.g., @Column(unique = true))
     @Test
     void save_duplicateEmail_violatesUniqueConstraint() {
         em.persistAndFlush(newUser("unique@cpp.edu", "Hx"));
         User dup = newUser("unique@cpp.edu", "Hy");
         org.junit.jupiter.api.Assertions.assertThrows(
             org.springframework.dao.DataIntegrityViolationException.class,
             () -> { users.saveAndFlush(dup); }
         );
     }
}
