package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.SessionWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionWordRepository extends JpaRepository<SessionWord, UUID> {
    List<SessionWord> findBySessionId(UUID sessionId);
}