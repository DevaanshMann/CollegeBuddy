package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false, length = 255)
    private String campusDomain; // e.g. "csun.edu"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AccountStatus status; // PENDING_VERIFICATION / ACTIVE / DEACTIVATED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Role role; // STUDENT / ADMIN
}
