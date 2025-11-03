package com.collegebuddy.domain;

import jakarta.persistence.*;

@Entity
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String campusDomain; // e.g. "csun.edu"
    private String displayName;  // e.g. "CSU Northridge"

    // getters/setters
}
