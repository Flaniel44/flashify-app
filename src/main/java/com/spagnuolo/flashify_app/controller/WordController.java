package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.Word;
import com.spagnuolo.flashify_app.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
/*
Handles three things:

POST /api/words — adds a word to a word bank, including translation, hint, and notes
GET /api/words/word-bank/{wordBankId} — lists all words in a word bank
GET /api/words/{id} — looks up a specific word

This is how you'll populate word banks before a session.
*/
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @PostMapping
    public ResponseEntity<Word> createWord(@RequestBody CreateWordRequest request) {
        Word word = wordService.createWord(
                request.wordBankId(),
                request.word(),
                request.translation(),
                request.hint(),
                request.notes()
        );
        return ResponseEntity.ok(word);
    }

    @GetMapping("/word-bank/{wordBankId}")
    public ResponseEntity<List<Word>> getWordsByWordBank(@PathVariable UUID wordBankId) {
        return ResponseEntity.ok(wordService.findByWordBankId(wordBankId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWord(@PathVariable UUID id) {
        return wordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateWordRequest(
            UUID wordBankId,
            String word,
            String translation,
            String hint,
            String notes
    ) {}

    @PutMapping("/{id}")
    public ResponseEntity<Word> updateWord(
            @PathVariable UUID id,
            @RequestBody UpdateWordRequest request
    ) {
        return ResponseEntity.ok(wordService.updateWord(
                id,
                request.word(),
                request.translation(),
                request.hint(),
                request.notes()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable UUID id) {
        wordService.deleteWord(id);
        return ResponseEntity.noContent().build();
    }

    public record UpdateWordRequest(String word, String translation, String hint, String notes) {}
}