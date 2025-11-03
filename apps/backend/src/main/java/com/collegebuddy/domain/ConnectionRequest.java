package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "connection_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_request_pair",
                columnNames = {"from_user_id", "to_user_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConnectionRequestStatus status; // PENDING / ACCEPTED / DECLINED
}
