package com.spagnuolo.flashify_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "session_words")
public class SessionWord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    private String revealedBy;

    private Boolean hintUsed = false;

    @CreationTimestamp
    private LocalDateTime revealedAt;
}