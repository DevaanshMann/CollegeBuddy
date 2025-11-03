package com.collegebuddy.domain;

import jakarta.persistence.*;

@Entity
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;
    private Long toUserId;

    @Enumerated(EnumType.STRING)
    private ConnectionRequestStatus status; // PENDING / ACCEPTED / DECLINED

    // getters/setters
}
