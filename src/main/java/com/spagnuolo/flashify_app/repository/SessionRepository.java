package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByInviteToken(UUID inviteToken);
    List<Session> findByTeacherId(UUID teacherId);
    List<Session> findByStudentId(UUID studentId);
    @Query("SELECT s FROM Session s WHERE s.status = 'active' AND s.startedAt < :cutoff")
    List<Session> findStaleActiveSessions(@Param("cutoff") LocalDateTime cutoff);
    @Query("SELECT s FROM Session s WHERE s.student.id = :studentId ORDER BY s.createdAt DESC LIMIT 1")
Optional<Session> findLastSessionByStudentId(@Param("studentId") UUID studentId);
}