package com.collegebuddy.repo;

import com.collegebuddy.domain.ConnectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {
    Optional<ConnectionRequest> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
}
