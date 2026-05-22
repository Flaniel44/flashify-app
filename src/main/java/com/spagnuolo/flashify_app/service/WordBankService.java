package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.Student;
import com.spagnuolo.flashify_app.entity.Word;
import com.spagnuolo.flashify_app.entity.WordBank;
import com.spagnuolo.flashify_app.repository.StudentRepository;
import com.spagnuolo.flashify_app.repository.WordBankRepository;
import com.spagnuolo.flashify_app.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordBankService {

    private final WordBankRepository wordBankRepository;
    private final StudentRepository studentRepository;
    private final WordRepository wordRepository;

    // Create a new word bank and optionally associate with a student
    @Transactional
    public WordBank createWordBank(UUID studentId, String name) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        WordBank wordBank = new WordBank();
        wordBank.setName(name);
        wordBank.getStudents().add(student);
        return wordBankRepository.save(wordBank);
    }

    // Get all word banks for a student
    @Transactional
    public List<WordBank> findByStudentId(UUID studentId) {
        return wordBankRepository.findByStudentId(studentId);
    }

    // Get all word banks
    @Transactional
    public List<WordBank> findAll() {
        return wordBankRepository.findAll();
    }

    @Transactional
    public Optional<WordBank> findById(UUID id) {
        return wordBankRepository.findById(id);
    }

    // Associate a word bank with a student
    @Transactional
    public WordBank associateStudent(UUID wordBankId, UUID studentId) {
        WordBank wordBank = wordBankRepository.findById(wordBankId)
                .orElseThrow(() -> new RuntimeException("Word bank not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        wordBank.getStudents().add(student);
        return wordBankRepository.save(wordBank);
    }

    // Unassociate a word bank from a student
    @Transactional
    public WordBank unassociateStudent(UUID wordBankId, UUID studentId) {
        WordBank wordBank = wordBankRepository.findById(wordBankId)
                .orElseThrow(() -> new RuntimeException("Word bank not found"));
        wordBank.getStudents().removeIf(s -> s.getId().equals(studentId));
        return wordBankRepository.save(wordBank);
    }

    // Duplicate a word bank with all its words and associations
    @Transactional
    public WordBank duplicate(UUID wordBankId) {
        WordBank original = wordBankRepository.findById(wordBankId)
                .orElseThrow(() -> new RuntimeException("Word bank not found"));

        // Create new word bank
        WordBank copy = new WordBank();
        copy.setName(original.getName() + " (copy)");
        copy.getStudents().addAll(original.getStudents());
        WordBank saved = wordBankRepository.save(copy);

        // Copy all words
        List<Word> originalWords = wordRepository.findByWordBankId(wordBankId);
        for (Word w : originalWords) {
            Word newWord = new Word();
            newWord.setWordBank(saved);
            newWord.setWord(w.getWord());
            newWord.setTranslation(w.getTranslation());
            newWord.setHint(w.getHint());
            newWord.setNotes(w.getNotes());
            wordRepository.save(newWord);
        }

        return saved;
    }

    // Update word bank name
    @Transactional
    public WordBank updateWordBank(UUID id, String name) {
        WordBank wordBank = wordBankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word bank not found"));
        wordBank.setName(name);
        return wordBankRepository.save(wordBank);
    }

    // Delete word bank
    public void deleteWordBank(UUID id) {
        wordBankRepository.deleteById(id);
    }

    @Transactional
    public List<WordBank> findByTeacherId(UUID teacherId) {
        return wordBankRepository.findByTeacherId(teacherId);
    }
}