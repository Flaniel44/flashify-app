package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.Word;
import com.spagnuolo.flashify_app.entity.WordBank;
import com.spagnuolo.flashify_app.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
// Handles creating individual words and looking them up by word bank. 
// Verifies the word bank exists before adding a word to it. Also carries 
// all four word fields — word, translation, hint, and notes — through to the database
@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final WordBankService wordBankService;

    public Word createWord(UUID wordBankId, String word, String translation, String hint, String notes) {
        WordBank wordBank = wordBankService.findById(wordBankId)
                .orElseThrow(() -> new RuntimeException("Word bank not found"));
        Word newWord = new Word();
        newWord.setWordBank(wordBank);
        newWord.setWord(word);
        newWord.setTranslation(translation);
        newWord.setHint(hint);
        newWord.setNotes(notes);
        return wordRepository.save(newWord);
    }

    public List<Word> findByWordBankId(UUID wordBankId) {
        return wordRepository.findByWordBankId(wordBankId);
    }

    public Optional<Word> findById(UUID id) {
        return wordRepository.findById(id);
    }

    public Word updateWord(UUID id, String word, String translation, String hint, String notes) {
        Word existing = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word not found"));
        existing.setWord(word);
        existing.setTranslation(translation);
        existing.setHint(hint);
        existing.setNotes(notes);
        return wordRepository.save(existing);
    }

    public void deleteWord(UUID id) {
        wordRepository.deleteById(id);
    }
}