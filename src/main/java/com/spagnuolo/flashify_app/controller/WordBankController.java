package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.WordBank;
import com.spagnuolo.flashify_app.service.WordBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
/*
GET /api/word-banks
Returns all word banks in the system regardless of which student they're associated with. This is used to populate the scrollable panel in the dashboard so you can browse and associate any word bank with the currently selected student.
GET /api/word-banks/student/{studentId}
Returns all word banks associated with a specific student. This is what populates the word bank list when you click on a student in the dashboard.
GET /api/word-banks/{id}
Returns a single word bank by its ID. Used when you need the full details of one specific word bank.
POST /api/word-banks
Creates a new word bank and immediately associates it with the student whose ID you pass in. So creating a word bank always starts with at least one student attached to it.
PUT /api/word-banks/{id}
Renames a word bank. Takes the new name in the request body and saves it.
POST /api/word-banks/{id}/associate/{studentId}
Links an existing word bank to a student. This is the key sharing endpoint — it adds a row to the student_word_banks join table without touching the word bank itself or any other students already linked to it.
DELETE /api/word-banks/{id}/unassociate/{studentId}
Removes the link between a word bank and a student. Deletes the row from student_word_banks for that specific pairing. The word bank itself is not deleted — it remains associated with any other students it was linked to.
POST /api/word-banks/{id}/duplicate
Creates a full copy of a word bank including all its words and all its student associations. The copy gets the same name with (copy) appended. Useful when you want a starting point for a new word bank that's similar to an existing one.
DELETE /api/word-banks/{id}
Permanently deletes a word bank and all its words. Because of the ON DELETE CASCADE we set up on student_word_banks, all associations are automatically cleaned up too.
*/
@RestController
@RequestMapping("/api/word-banks")
@RequiredArgsConstructor
public class WordBankController {

    private final WordBankService wordBankService;

    @PostMapping
    public ResponseEntity<WordBank> createWordBank(@RequestBody CreateWordBankRequest request) {
        return ResponseEntity.ok(wordBankService.createWordBank(request.studentId(), request.name()));
    }

    @GetMapping
    public ResponseEntity<List<WordBank>> getAllWordBanks() {
        return ResponseEntity.ok(wordBankService.findAll());
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

    @PutMapping("/{id}")
    public ResponseEntity<WordBank> updateWordBank(
            @PathVariable UUID id,
            @RequestBody UpdateWordBankRequest request
    ) {
        return ResponseEntity.ok(wordBankService.updateWordBank(id, request.name()));
    }

    @PostMapping("/{id}/associate/{studentId}")
    public ResponseEntity<WordBank> associateStudent(
            @PathVariable UUID id,
            @PathVariable UUID studentId
    ) {
        return ResponseEntity.ok(wordBankService.associateStudent(id, studentId));
    }

    @DeleteMapping("/{id}/unassociate/{studentId}")
    public ResponseEntity<WordBank> unassociateStudent(
            @PathVariable UUID id,
            @PathVariable UUID studentId
    ) {
        return ResponseEntity.ok(wordBankService.unassociateStudent(id, studentId));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<WordBank> duplicate(@PathVariable UUID id) {
        return ResponseEntity.ok(wordBankService.duplicate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWordBank(@PathVariable UUID id) {
        wordBankService.deleteWordBank(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateWordBankRequest(UUID studentId, String name) {}
    public record UpdateWordBankRequest(String name) {}
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<WordBank>> getWordBanksByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(wordBankService.findByTeacherId(teacherId));
    }
}