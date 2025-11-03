package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "connections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_connection_pair",
                columnNames = {"user_a_id", "user_b_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We store both sides of the friendship.
    // Convention: userAId < userBId when created so pairs are unique.
    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    @Column(name = "user_b_id", nullable = false)
    private Long userBId;
}
