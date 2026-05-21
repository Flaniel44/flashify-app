package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.WordBank;
import com.spagnuolo.flashify_app.service.WordBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/*
Handles three things:

POST /api/word-banks — creates a new word bank for a student
GET /api/word-banks/student/{studentId} — lists all word banks for a student
GET /api/word-banks/{id} — looks up a specific word bank

A student can have multiple word banks — for example "Week 1 Vocabulary" and "Week 2 Vocabulary".
*/

@RestController
@RequestMapping("/api/word-banks")
@RequiredArgsConstructor
public class WordBankController {

    private final WordBankService wordBankService;

    @PostMapping
    public ResponseEntity<WordBank> createWordBank(@RequestBody CreateWordBankRequest request) {
        WordBank wordBank = wordBankService.createWordBank(request.studentId(), request.name());
        return ResponseEntity.ok(wordBank);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<WordBank>> getWordBanksByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(wordBankService.findByStudentId(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordBank> getWordBank(@PathVariable UUID id) {
        return wordBankService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateWordBankRequest(UUID studentId, String name) {}
}