package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "verification_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // random token string (UUID-like)
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    // which user this activation token belongs to
    @Column(nullable = false)
    private Long userId;

    // token expiry cutoff
    @Column(nullable = false)
    private Instant expiresAt;
}
