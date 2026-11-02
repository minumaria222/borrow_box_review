package com.borrowbox.controller;

import com.borrowbox.dto.StudentCreateRequest;
import com.borrowbox.dto.StudentResponse;
import com.borrowbox.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService students;

    public StudentController(StudentService students) { this.students = students; }

    @GetMapping
    public List<StudentResponse> list() { return students.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentCreateRequest form) { return students.create(form); }
}
