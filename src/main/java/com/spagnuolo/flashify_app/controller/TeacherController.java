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

    @PostMapping
    public ResponseEntity<Teacher> createTeacher(@RequestBody CreateTeacherRequest request) {
        Teacher teacher = teacherService.createTeacher(request.name(), request.email());
        return ResponseEntity.ok(teacher);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacher(@PathVariable UUID id) {
        return teacherService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateTeacherRequest(String name, String email) {}
}