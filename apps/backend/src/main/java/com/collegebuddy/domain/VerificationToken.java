package com.collegebuddy.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Long userId;
    private Instant expiresAt;

    // getters/setters
}
