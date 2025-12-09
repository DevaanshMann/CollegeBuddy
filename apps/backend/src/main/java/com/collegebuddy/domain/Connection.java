package com.collegebuddy.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.Instant;

@Entity
@Table(
        name = "connections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_connection_pair",
                columnNames = {"user_a_id", "user_b_id"}
        )
)
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    @Column(name = "user_b_id", nullable = false)
    private Long userBId;

    @Column(nullable = false)
    private Instant createdAt;

    public Connection() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserAId() { return userAId; }
    public void setUserAId(Long userAId) { this.userAId = userAId; }

    public Long getUserBId() { return userBId; }
    public void setUserBId(Long userBId) { this.userBId = userBId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
