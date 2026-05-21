package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WordRepository extends JpaRepository<Word, UUID> {
    List<Word> findByWordBankId(UUID wordBankId);
}