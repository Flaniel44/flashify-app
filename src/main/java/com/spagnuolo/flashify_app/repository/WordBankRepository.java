package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.WordBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WordBankRepository extends JpaRepository<WordBank, UUID> {
    List<WordBank> findByStudentId(UUID studentId);
}