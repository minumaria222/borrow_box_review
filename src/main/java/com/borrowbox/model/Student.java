package com.borrowbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, unique = true)
    private String email;

    protected Student() {}

    public Student(String name, String department, String email) {
        this.name = name;
        this.department = department;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getEmail() { return email; }
    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setEmail(String email) { this.email = email; }
}
