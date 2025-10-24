package com.collegebuddy.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "schools", uniqueConstraints = @UniqueConstraint(columnNames = "domain"))
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String domain;

    @Column(nullable = false)
    private String name;

    // --- constructors ---
    public School() {}

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // used in tests/mocks

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
