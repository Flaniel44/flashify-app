package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.Teacher;
import com.spagnuolo.flashify_app.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Registration code — keep this secret!
    @Value("${flashify.registration-code}")
    private String registrationCodeConfig;

    public Teacher register(String name, String username, String password, String registrationCode, String email) {
    if (!registrationCodeConfig.equals(registrationCode)) {
        throw new RuntimeException("Invalid registration code");
    }
    if (teacherRepository.findByUsername(username).isPresent()) {
        throw new RuntimeException("Username already taken");
    }
    Teacher teacher = new Teacher();
    teacher.setName(name);
    teacher.setUsername(username);
    teacher.setPassword(passwordEncoder.encode(password));
    teacher.setEmail(email);
    return teacherRepository.save(teacher);
}

    public Teacher login(String username, String password) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        if (!passwordEncoder.matches(password, teacher.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        // Never return the password to the frontend
        teacher.setPassword(null);
        return teacher;
    }

    public Optional<Teacher> findById(UUID id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> findByUsername(String username) {
        return teacherRepository.findByUsername(username);
    }
}