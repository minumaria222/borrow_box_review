package com.borrowbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expectedReturnDate;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    protected BorrowRequest() {}

    public BorrowRequest(Student student, Item item, LocalDate startDate, LocalDate expectedReturnDate, String message) {
        this.student = student;
        this.item = item;
        this.startDate = startDate;
        this.expectedReturnDate = expectedReturnDate;
        this.message = message;
    }

    @PrePersist
    void onCreate() { requestedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Item getItem() { return item; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public String getMessage() { return message; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setStatus(RequestStatus status) { this.status = status; }
}
