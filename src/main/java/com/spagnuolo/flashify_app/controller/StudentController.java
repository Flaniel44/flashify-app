package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.Student;
import com.spagnuolo.flashify_app.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
/*
Handles three things:

POST /api/students — creates a new student under a teacher
GET /api/students/teacher/{teacherId} — lists all your students
GET /api/students/{id} — looks up a specific student

You'll use this to manage your roster of students.
*/
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody CreateStudentRequest request) {
        Student student = studentService.createStudent(
                request.teacherId(),
                request.name()
        );
        return ResponseEntity.ok(student);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Student>> getStudentsByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(studentService.findByTeacherId(teacherId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable UUID id) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateStudentRequest(UUID teacherId, String name) {}

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable UUID id,
            @RequestBody UpdateStudentRequest request
    ) {
        return ResponseEntity.ok(studentService.updateStudent(id, request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    public record UpdateStudentRequest(String name) {}
}