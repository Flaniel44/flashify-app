package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.*;
import com.spagnuolo.flashify_app.repository.SessionRepository;
import com.spagnuolo.flashify_app.repository.SessionWordRepository;
import com.spagnuolo.flashify_app.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
/*
Three main responsibilities:
createSession — a teacher kicks off a session for a specific student using a specific word bank. 
Sets the initial state: status is waiting, turn starts with the teacher, word index starts at 0.
joinSession — a student arrives via their invite URL. Finds the session by the invite token, 
flips the status from waiting to active, and stamps the start time.
revealWord — the heart of the app. When either participant reveals a word it:

Looks up the current word by index
Records it in session_words with who revealed it and whether a hint was used
Advances the word index by 1
Flips the turn to the other person
If there are no words left, marks the session as completed
*/
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionWordRepository sessionWordRepository;
    private final WordRepository wordRepository;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final WordBankService wordBankService;

    // Teacher creates a session for a student
    public Session createSession(UUID teacherId, UUID studentId, UUID wordBankId) {
        Teacher teacher = teacherService.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        WordBank wordBank = wordBankService.findById(wordBankId)
                .orElseThrow(() -> new RuntimeException("Word bank not found"));

        Session session = new Session();
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setWordBank(wordBank);
        session.setStatus("waiting");
        session.setCurrentWordIndex(0);
        session.setCurrentTurn("teacher");
        return sessionRepository.save(session);
    }

    // Student joins via invite token
    public Session joinSession(UUID inviteToken) {
        Session session = sessionRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (session.getStatus().equals("waiting")) {
            session.setStatus("active");
            session.setStartedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }
        return session;
    }

    // Reveal the word to both players
@Transactional
public Session revealWord(UUID sessionId, String revealedBy) {
    Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

    session.setWordRevealed(true);

    // Record the revealed word
    List<Word> words = wordRepository.findByWordBankId(session.getWordBank().getId());
    Word currentWord = words.get(session.getCurrentWordIndex());

    SessionWord sessionWord = new SessionWord();
    sessionWord.setSession(session);
    sessionWord.setWord(currentWord);
    sessionWord.setRevealedBy(revealedBy);
    sessionWord.setHintUsed(session.getHintRevealed());
    sessionWordRepository.save(sessionWord);

    return sessionRepository.save(session);
}

// Reveal the hint to both players
@Transactional
public Session revealHint(UUID sessionId) {
    Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

    session.setHintRevealed(true);
    return sessionRepository.save(session);
}

// Advance to the next word and switch turn
@Transactional
public Session nextWord(UUID sessionId, String currentTurn) {
    Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

    List<Word> words = wordRepository.findByWordBankId(session.getWordBank().getId());

    session.setCurrentWordIndex(session.getCurrentWordIndex() + 1);
    session.setCurrentTurn(currentTurn.equals("teacher") ? "student" : "teacher");
    session.setWordRevealed(false);
    session.setHintRevealed(false);

    if (session.getCurrentWordIndex() >= words.size()) {
        session.setStatus("completed");
        session.setCompletedAt(LocalDateTime.now());
    }

        return sessionRepository.save(session);
    }

    public Optional<Session> findByInviteToken(UUID inviteToken) {
        return sessionRepository.findByInviteToken(inviteToken);
    }

    public Optional<Session> findById(UUID id) {
        return sessionRepository.findById(id);
    }

    public List<Session> findByTeacherId(UUID teacherId) {
        return sessionRepository.findByTeacherId(teacherId);
    }

    @Transactional
    public Session endSession(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus("completed");
        session.setCompletedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }
}