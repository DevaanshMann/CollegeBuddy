package com.collegebuddy.domain;

import jakarta.persistence.*;

@Entity
public class Profile {

    @Id
    private Long userId; // 1:1 with User

    private String displayName;
    private String bio;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    // getters/setters
}
