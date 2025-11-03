package com.collegebuddy.repo;

import com.collegebuddy.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // userId is the PK
}
