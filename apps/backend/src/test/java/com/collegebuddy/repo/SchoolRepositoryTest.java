//package com.collegebuddy.repo;
//
//import com.collegebuddy.domain.School;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@ActiveProfiles("test")
//class SchoolRepositoryTest {
//
//    private final SchoolRepository schools;
//
//    SchoolRepositoryTest(@Autowired SchoolRepository schools) {
//        this.schools = schools;
//    }
//
//    @Test
//    void findByDomain_returnsSavedSchool() {
//        var s = new School();
//        s.setDomain("abc.edu");
//        s.setName("ABC University");
//        schools.save(s);
//
//        assertThat(schools.findByDomain("abc.edu")).isPresent();
//        assertThat(schools.findByDomain("none.edu")).isEmpty();
//    }
//}


package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // H2 in-memory
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class SchoolRepositoryTest {

    @Autowired
    private SchoolRepository repo;

    private School csun;
    private School cpp;
    private School sdsu;

    @BeforeEach
    void seed() {
        csun = new School();
        csun.setName("California State University, Northridge");
        csun.setDomain("csun.edu");
        repo.save(csun);

        cpp = new School();
        cpp.setName("Cal Poly Pomona");
        cpp.setDomain("cpp.edu");
        repo.save(cpp);

        sdsu = new School();
        sdsu.setName("San Diego State University");
        sdsu.setDomain("sdsu.edu");
        repo.saveAndFlush(sdsu);
    }

    // ---------------------------------------------------------------------
    // findByDomain — present
    // ---------------------------------------------------------------------

    @Test
    void findByDomain_returnsMatchWhenPresent() {
        Optional<School> found = repo.findByDomain("csun.edu");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).contains("Northridge");
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void findByDomain_present_caseMustMatch_exactOnly() {
        // same domain in upper-case should not match unless ignoring case
        assertThat(repo.findByDomain("CSUN.EDU")).isEmpty();
    }

    @Test
    void findByDomain_present_trimmedQueryDoesNotMatch() {
        assertThat(repo.findByDomain(" csun.edu ")).isEmpty();
    }

    // ---------------------------------------------------------------------
    // findByDomain — absent
    // ---------------------------------------------------------------------

    @Test
    void findByDomain_returnsEmptyWhenAbsent() {
        assertThat(repo.findByDomain("nonexistent.edu")).isEmpty();
    }

    @Test
    void findByDomain_subdomainDoesNotMatch() {
        assertThat(repo.findByDomain("mail.cpp.edu")).isEmpty();
    }

    @Test
    void findByDomain_emptyStringDoesNotMatch() {
        assertThat(repo.findByDomain("")).isEmpty();
    }

    // ---------------------------------------------------------------------
    // save / persist basics
    // ---------------------------------------------------------------------

    @Test
    void save_persistsAndAssignsId() {
        School ucla = new School();
        ucla.setName("UCLA");
        ucla.setDomain("ucla.edu");
        School saved = repo.saveAndFlush(ucla);

        assertThat(saved.getId()).isNotNull();
        assertThat(repo.findById(saved.getId())).isPresent();
        assertThat(repo.findByDomain("ucla.edu")).isPresent();
    }

    @Test
    void save_incrementsCount_andRoundTrips() {
        long before = repo.count();
        School uci = new School();
        uci.setName("UCI");
        uci.setDomain("uci.edu");
        repo.saveAndFlush(uci);

        assertThat(repo.count()).isEqualTo(before + 1);
        assertThat(repo.findByDomain("uci.edu")).isPresent();
    }

    @Test
    void save_multipleEntities_persistsAll() {
        School ucsd = new School();
        ucsd.setName("UCSD");
        ucsd.setDomain("ucsd.edu");

        School ucsb = new School();
        ucsb.setName("UCSB");
        ucsb.setDomain("ucsb.edu");

        repo.saveAll(List.of(ucsd, ucsb));
        repo.flush();

        assertThat(repo.findByDomain("ucsd.edu")).isPresent();
        assertThat(repo.findByDomain("ucsb.edu")).isPresent();
    }

    // ---------------------------------------------------------------------
    // update round-trip
    // ---------------------------------------------------------------------

    @Test
    void update_roundTripPersistsChanges() {
        School loaded = repo.findByDomain("cpp.edu").orElseThrow();
        loaded.setName("California State Polytechnic University, Pomona");
        repo.saveAndFlush(loaded);

        School reloaded = repo.findById(loaded.getId()).orElseThrow();
        assertThat(reloaded.getName()).contains("Polytechnic");
    }

    @Test
    void update_changeDomain_oldDomainNotFound_newDomainFound() {
        School loaded = repo.findByDomain("sdsu.edu").orElseThrow();
        loaded.setDomain("sandiegostate.edu");
        repo.saveAndFlush(loaded);

        assertThat(repo.findByDomain("sdsu.edu")).isEmpty();
        assertThat(repo.findByDomain("sandiegostate.edu")).isPresent();
    }

    @Test
    void update_changeName_only_keepsDomainUnchanged() {
        School loaded = repo.findByDomain("csun.edu").orElseThrow();
        loaded.setName("CSU Northridge");
        repo.saveAndFlush(loaded);

        assertThat(repo.findByDomain("csun.edu")).isPresent()
                .get()
                .extracting(School::getName)
                .isEqualTo("CSU Northridge");
    }

    // ---------------------------------------------------------------------
    // delete variants
    // ---------------------------------------------------------------------

    @Test
    void delete_removesEntity() {
        Long id = csun.getId();
        assertThat(repo.findById(id)).isPresent();

        repo.deleteById(id);
        repo.flush();

        assertThat(repo.findById(id)).isEmpty();
        assertThat(repo.findByDomain("csun.edu")).isEmpty();
    }

    @Test
    void delete_byEntity_removesEntity() {
        School toDelete = repo.findByDomain("cpp.edu").orElseThrow();
        repo.delete(toDelete);
        repo.flush();
        assertThat(repo.findByDomain("cpp.edu")).isEmpty();
    }

    @Test
    void delete_all_clearsRepository() {
        repo.deleteAll();
        repo.flush();
        assertThat(repo.count()).isZero();
    }

    // ---------------------------------------------------------------------
    // sorting
    // ---------------------------------------------------------------------

    @Test
    void findAll_withSortingByDomainAscending() {
        List<School> sorted = repo.findAll(Sort.by(Sort.Direction.ASC, "domain"));
        assertThat(sorted).extracting(School::getDomain)
                .containsExactly("cpp.edu", "csun.edu", "sdsu.edu");
    }

    @Test
    void findAll_withSortingByDomainDescending() {
        List<School> sorted = repo.findAll(Sort.by(Sort.Direction.DESC, "domain"));
        assertThat(sorted).extracting(School::getDomain)
                .containsExactly("sdsu.edu", "csun.edu", "cpp.edu");
    }

    @Test
    void findAll_withSortingByNameAscending() {
        List<School> sorted = repo.findAll(Sort.by(Sort.Direction.ASC, "name"));
        // Alphabetically: Cal Poly Pomona, California State University, Northridge, San Diego State University
        assertThat(sorted).extracting(School::getName)
                .containsExactly(
                        "Cal Poly Pomona",
                        "California State University, Northridge",
                        "San Diego State University"
                );
    }

    // ---------------------------------------------------------------------
    // paging
    // ---------------------------------------------------------------------

    @Test
    void findAll_withPaging_returnsCorrectSlice() {
        Page<School> page0 = repo.findAll(PageRequest.of(0, 2, Sort.by("domain")));
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getContent()).extracting(School::getDomain)
                .containsExactly("cpp.edu", "csun.edu");

        Page<School> page1 = repo.findAll(PageRequest.of(1, 2, Sort.by("domain")));
        assertThat(page1.getContent()).extracting(School::getDomain)
                .containsExactly("sdsu.edu");
    }

    @Test
    void findAll_withPaging_beyondLastPage_returnsEmptyContent() {
        Page<School> page = repo.findAll(PageRequest.of(5, 2, Sort.by("domain")));
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findAll_withPaging_pageSizeOne_iteratesInOrder() {
        Page<School> p0 = repo.findAll(PageRequest.of(0, 1, Sort.by("domain")));
        Page<School> p1 = repo.findAll(PageRequest.of(1, 1, Sort.by("domain")));
        Page<School> p2 = repo.findAll(PageRequest.of(2, 1, Sort.by("domain")));

        assertThat(p0.getContent()).extracting(School::getDomain).containsExactly("cpp.edu");
        assertThat(p1.getContent()).extracting(School::getDomain).containsExactly("csun.edu");
        assertThat(p2.getContent()).extracting(School::getDomain).containsExactly("sdsu.edu");
    }

    // ---------------------------------------------------------------------
    // exact-match behavior
    // ---------------------------------------------------------------------

    @Test
    void findByDomain_isExactMatch_notTrimmedOrCaseFolded() {
        // saved as lowercase without spaces
        assertThat(repo.findByDomain(" csun.edu ")).isEmpty();
        assertThat(repo.findByDomain("CSUN.EDU")).isEmpty();
    }

    @Test
    void findByDomain_similarButDifferentTld_doesNotMatch() {
        assertThat(repo.findByDomain("csun.com")).isEmpty();
    }

    @Test
    void findByDomain_savedWithWhitespace_onlyMatchesExact() {
        // Save a record that actually includes spaces in the domain field (discouraged, but tests exactness)
        School odd = new School();
        odd.setName("Odd School");
        odd.setDomain(" odd.edu ");
        repo.saveAndFlush(odd);

        // Exact query (with spaces) matches
        assertThat(repo.findByDomain(" odd.edu ")).isPresent();

        // Trimmed query does NOT match
        assertThat(repo.findByDomain("odd.edu")).isEmpty();
    }
}
