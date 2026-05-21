package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.Student;
import com.spagnuolo.flashify_app.entity.WordBank;
import com.spagnuolo.flashify_app.repository.WordBankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordBankService {

    private final WordBankRepository wordBankRepository;
    private final StudentService studentService;

    public WordBank createWordBank(UUID studentId, String name) {
        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        WordBank wordBank = new WordBank();
        wordBank.setStudent(student);
        wordBank.setName(name);
        return wordBankRepository.save(wordBank);
    }

    public List<WordBank> findByStudentId(UUID studentId) {
        return wordBankRepository.findByStudentId(studentId);
    }

    public Optional<WordBank> findById(UUID id) {
        return wordBankRepository.findById(id);
    }
}