package com.spagnuolo.flashify_app.repository;

import com.spagnuolo.flashify_app.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findByTeacherId(UUID teacherId);
}