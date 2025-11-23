package com.collegebuddy.repo;

import com.collegebuddy.domain.ConnectionRequest;
import com.collegebuddy.domain.ConnectionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {

    boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, ConnectionRequestStatus status);

    List<ConnectionRequest> findByToUserIdAndStatus(Long toUserId, ConnectionRequestStatus status);

    List<ConnectionRequest> findByFromUserIdAndStatus(Long fromUserId, ConnectionRequestStatus status);

    void deleteByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
}
