package com.borrowbox.dto;

import com.borrowbox.model.Student;

public record StudentResponse(Long id, String name, String department, String email) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getDepartment(), student.getEmail());
    }
}
