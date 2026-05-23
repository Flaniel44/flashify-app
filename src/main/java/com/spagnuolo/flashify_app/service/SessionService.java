package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.*;
import com.spagnuolo.flashify_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionWordRepository sessionWordRepository;
    private final WordRepository wordRepository;
    private final SessionWordOrderRepository sessionWordOrderRepository;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final WordBankService wordBankService;

    @Transactional
    public Session createSession(UUID teacherId, UUID studentId, UUID wordBankId, String sessionType, boolean shuffled) {
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
        session.setSessionType(sessionType);
        session.setShuffled(shuffled);

        // Set initial turn based on session type
        if (sessionType.equals("student_only")) {
            session.setCurrentTurn("student");
        } else {
            session.setCurrentTurn("teacher");
        }

        Session saved = sessionRepository.save(session);

        // Build word order
        List<Word> words = wordRepository.findByWordBankId(wordBankId);
        if (shuffled) {
            List<Word> shuffledWords = new ArrayList<>(words);
            Collections.shuffle(shuffledWords);
            words = shuffledWords;
        }

        for (int i = 0; i < words.size(); i++) {
            SessionWordOrder order = new SessionWordOrder();
            order.setSession(saved);
            order.setWord(words.get(i));
            order.setPosition(i);
            sessionWordOrderRepository.save(order);
        }

        return saved;
    }

    @Transactional
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

    @Transactional
    public Session revealWord(UUID sessionId, String revealedBy) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setWordRevealed(true);

        List<SessionWordOrder> wordOrder = sessionWordOrderRepository.findBySessionIdOrderByPosition(sessionId);
        Word currentWord = wordOrder.get(session.getCurrentWordIndex()).getWord();

        SessionWord sessionWord = new SessionWord();
        sessionWord.setSession(session);
        sessionWord.setWord(currentWord);
        sessionWord.setRevealedBy(revealedBy);
        sessionWord.setHintUsed(session.getHintRevealed());
        sessionWordRepository.save(sessionWord);

        return sessionRepository.save(session);
    }

    @Transactional
    public Session revealHint(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setHintRevealed(true);
        return sessionRepository.save(session);
    }

    @Transactional
    public Session nextWord(UUID sessionId, String currentTurn) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<SessionWordOrder> wordOrder = sessionWordOrderRepository.findBySessionIdOrderByPosition(sessionId);

        session.setCurrentWordIndex(session.getCurrentWordIndex() + 1);
        session.setWordRevealed(false);
        session.setHintRevealed(false);

        // Alternate turn only for alternating session type
        if (session.getSessionType().equals("alternating")) {
            session.setCurrentTurn(currentTurn.equals("teacher") ? "student" : "teacher");
        }
        // teacher_only and student_only keep the same turn

        if (session.getCurrentWordIndex() >= wordOrder.size()) {
            session.setStatus("completed");
            session.setCompletedAt(LocalDateTime.now());
        }

        return sessionRepository.save(session);
    }

    @Transactional
    public Session endSession(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus("completed");
        session.setCompletedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public List<Word> getSessionWords(UUID sessionId) {
        return sessionWordOrderRepository.findBySessionIdOrderByPosition(sessionId)
                .stream()
                .map(SessionWordOrder::getWord)
                .toList();
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
    public void cleanUpStaleSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
        List<Session> staleSessions = sessionRepository.findStaleActiveSessions(cutoff);
        staleSessions.forEach(s -> {
            s.setStatus("completed");
            s.setCompletedAt(LocalDateTime.now());
        });
        sessionRepository.saveAll(staleSessions);
        System.out.println(">>> Cleaned up " + staleSessions.size() + " stale sessions");
    }
}