package com.collegebuddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which conversation this message belongs to
    @Column(nullable = false)
    private Long conversationId;

    // who sent it
    @Column(nullable = false)
    private Long senderUserId;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant sentAt;
}
