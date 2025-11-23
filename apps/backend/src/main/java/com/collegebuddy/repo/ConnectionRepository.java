package com.collegebuddy.repo;

import com.collegebuddy.domain.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByUserAIdOrUserBId(Long userAId, Long userBId);

    boolean existsByUserAIdAndUserBId(Long userAId, Long userBId);

    @Modifying
    void deleteByUserAIdAndUserBId(Long userAId, Long userBId);
}
