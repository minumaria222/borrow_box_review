package com.borrowbox.service;

import com.borrowbox.dto.StudentCreateRequest;
import com.borrowbox.dto.StudentResponse;
import com.borrowbox.model.Student;
import com.borrowbox.repository.StudentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentService {
    private final StudentRepository students;

    public StudentService(StudentRepository students) { this.students = students; }

    @Transactional(readOnly = true)
    public List<StudentResponse> list() {
        return students.findAll().stream().map(StudentResponse::from).toList();
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest form) {
        if (students.findByEmail(form.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A student with this email already exists.");
        }
        return StudentResponse.from(students.save(new Student(form.name(), form.department(), form.email())));
    }

    public Student requireStudent(Long id) {
        return students.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
    }
}
