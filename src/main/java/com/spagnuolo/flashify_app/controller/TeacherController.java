package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.Teacher;
import com.spagnuolo.flashify_app.service.JwtService;
import com.spagnuolo.flashify_app.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final JwtService jwtService;

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
            String token = jwtService.generateToken(teacher.getId(), teacher.getUsername());
            return ResponseEntity.ok(Map.of("teacher", teacher, "token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Teacher teacher = teacherService.login(request.username(), request.password());
            String token = jwtService.generateToken(teacher.getId(), teacher.getUsername());
            return ResponseEntity.ok(Map.of("teacher", teacher, "token", token));
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

    public record RegisterRequest(String name, String username, String password, String registrationCode, String email) {}
    public record LoginRequest(String username, String password) {}
}