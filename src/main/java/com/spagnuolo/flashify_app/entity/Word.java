package com.spagnuolo.flashify_app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "word_bank_id", nullable = false)
    private WordBank wordBank;

    @Column(nullable = false)
    private String word;

    private String translation;

    private String hint;

    private String notes;
}