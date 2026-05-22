package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.WordBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WordBankRepository extends JpaRepository<WordBank, UUID> {

    @Query("SELECT wb FROM WordBank wb JOIN wb.students s WHERE s.id = :studentId")
    List<WordBank> findByStudentId(@Param("studentId") UUID studentId);
}