package com.collegebuddy.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long conversationId;
    private Long senderUserId;
    private String content;
    private Instant sentAt;

    // getters/setters
}
