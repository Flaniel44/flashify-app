package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.Teacher;
import com.spagnuolo.flashify_app.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


//handles creating a teacher account and looking one up either by their ID or email
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public Teacher createTeacher(String name, String email) {
        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setEmail(email);
        return teacherRepository.save(teacher);
    }

    public Optional<Teacher> findById(UUID id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> findByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }
}