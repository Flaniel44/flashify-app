package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByInviteToken(UUID inviteToken);
    List<Session> findByTeacherId(UUID teacherId);
    List<Session> findByStudentId(UUID studentId);
}