package com.spagnuolo.flashify_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "word_bank_id", nullable = false)
    private WordBank wordBank;

    @Column(nullable = false)
    private String status = "waiting";

    private Integer currentWordIndex = 0;
    private String currentTurn;

    @Column(nullable = false, unique = true)
    private UUID inviteToken;

    private Boolean wordRevealed = false;
    private Boolean hintRevealed = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (inviteToken == null) {
            inviteToken = UUID.randomUUID();
        }
    }

    private String sessionType = "alternating";
    private Boolean shuffled = false;
}