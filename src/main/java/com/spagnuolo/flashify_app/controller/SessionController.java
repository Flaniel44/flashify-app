package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.Session;
import com.spagnuolo.flashify_app.entity.SessionWord;
import com.spagnuolo.flashify_app.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
/*
The most important controller — handles the live lesson flow:

POST /api/sessions — teacher creates a session for a student with a specific word bank, generates the invite token
POST /api/sessions/join/{inviteToken} — student clicks their invite URL, this flips the session from waiting to active
POST /api/sessions/{sessionId}/reveal — either participant reveals the current word, records it, and advances the turn
GET /api/sessions/teacher/{teacherId} — lists all sessions you've run
GET /api/sessions/{id} — looks up a specific session
*/
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // Teacher creates a session
    @PostMapping
    public ResponseEntity<Session> createSession(@RequestBody CreateSessionRequest request) {
        Session session = sessionService.createSession(
                request.teacherId(),
                request.studentId(),
                request.wordBankId()
        );
        return ResponseEntity.ok(session);
    }

    // Student joins via invite token in URL
    @PostMapping("/join/{inviteToken}")
    public ResponseEntity<Session> joinSession(@PathVariable UUID inviteToken) {
        Session session = sessionService.joinSession(inviteToken);
        return ResponseEntity.ok(session);
    }

    // Reveal current word
    @PostMapping("/{sessionId}/reveal")
    public ResponseEntity<Session> revealWord(
            @PathVariable UUID sessionId,
            @RequestBody RevealWordRequest request
    ) {
        Session session = sessionService.revealWord(
                sessionId,
                request.revealedBy()
        );
        return ResponseEntity.ok(session);
    }

    // Get all sessions for a teacher
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Session>> getSessionsByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(sessionService.findByTeacherId(teacherId));
    }

    // Get session by ID
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable UUID id) {
        return sessionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateSessionRequest(UUID teacherId, UUID studentId, UUID wordBankId) {}
    public record RevealWordRequest(String revealedBy, boolean hintUsed) {}
}