package com.collegebuddy.repo;

import com.collegebuddy.domain.Group;
import com.collegebuddy.domain.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findByCampusDomain(String campusDomain, Pageable pageable);

    Page<Group> findByCampusDomainAndVisibility(String campusDomain, Visibility visibility, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.campusDomain = :campusDomain " +
           "AND LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Group> searchByCampus(@Param("campusDomain") String campusDomain,
                                @Param("query") String query,
                                Pageable pageable);
}
