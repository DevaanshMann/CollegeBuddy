package com.collegebuddy.repo;

import com.collegebuddy.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByCampusDomain(String campusDomain);
}
