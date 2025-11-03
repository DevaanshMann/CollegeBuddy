package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    private Long userId; // reuse User.id as PK (1:1)

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 1000)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility; // PUBLIC / PRIVATE
}
