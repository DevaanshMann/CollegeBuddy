package com.collegebuddy.repo;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByCampusDomainAndStatus(String campusDomain, AccountStatus status);

    long countByStatus(AccountStatus status);
}
