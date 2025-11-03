package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // domain we trust for signup, ex: "csun.edu"
    @Column(nullable = false, unique = true, length = 255)
    private String campusDomain;

    // human-readable name, ex: "California State University, Northridge"
    @Column(nullable = false, length = 255)
    private String displayName;
}
