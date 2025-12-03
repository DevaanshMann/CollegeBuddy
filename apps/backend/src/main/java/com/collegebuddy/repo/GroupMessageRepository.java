package com.collegebuddy.repo;

import com.collegebuddy.domain.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    @Query("SELECT m FROM GroupMessage m WHERE m.groupId = :groupId ORDER BY m.sentAt ASC")
    List<GroupMessage> findByGroupIdOrderBySentAtAsc(@Param("groupId") Long groupId);
}
