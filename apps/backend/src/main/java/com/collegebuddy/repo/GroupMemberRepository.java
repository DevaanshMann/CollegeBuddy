package com.collegebuddy.repo;

import com.collegebuddy.domain.GroupMember;
import com.collegebuddy.domain.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    List<GroupMember> findByGroupIdAndRole(Long groupId, GroupRole role);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
