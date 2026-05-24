package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.Teacher;
import com.spagnuolo.flashify_app.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
/*
Handles two things:

POST /api/teachers — creates a new teacher account by accepting a name and email
GET /api/teachers/{id} — looks up a teacher by their ID

This will mostly be used during initial setup, since there's really only one teacher.
*/

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    try {
        Teacher teacher = teacherService.register(
                request.name(),
                request.username(),
                request.password(),
                request.registrationCode(),
                request.email()
        );
        return ResponseEntity.ok(teacher);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

public record RegisterRequest(String name, String username, String password, String registrationCode, String email) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Teacher teacher = teacherService.login(request.username(), request.password());
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacher(@PathVariable UUID id) {
        return teacherService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    public record LoginRequest(String username, String password) {}
}