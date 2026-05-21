package com.spagnuolo.flashify_app.service;

import com.spagnuolo.flashify_app.entity.Student;
import com.spagnuolo.flashify_app.entity.Teacher;
import com.spagnuolo.flashify_app.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Handles creating students and looking them up. The key thing it does beyond the 
// repository is verify the teacher exists before creating a student under them. 
// You can't create a student without a valid teacher — that rule lives here.
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeacherService teacherService;

    public Student createStudent(UUID teacherId, String name, String email) {
        Teacher teacher = teacherService.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = new Student();
        student.setTeacher(teacher);
        student.setName(name);
        student.setEmail(email);
        return studentRepository.save(student);
    }

    public List<Student> findByTeacherId(UUID teacherId) {
        return studentRepository.findByTeacherId(teacherId);
    }

    public Optional<Student> findById(UUID id) {
        return studentRepository.findById(id);
    }
}