package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.SessionWordOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionWordOrderRepository extends JpaRepository<SessionWordOrder, Long> {
    List<SessionWordOrder> findBySessionIdOrderByPosition(UUID sessionId);
}