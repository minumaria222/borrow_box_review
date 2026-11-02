package com.borrowbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String conditionLabel;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private int maxBorrowDays;

    @Column(length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Student owner;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Item() {}

    public Item(String name, String category, String description, String conditionLabel, String location,
                int maxBorrowDays, String imageUrl, Student owner) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.conditionLabel = conditionLabel;
        this.location = location;
        this.maxBorrowDays = maxBorrowDays;
        this.imageUrl = imageUrl;
        this.owner = owner;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getConditionLabel() { return conditionLabel; }
    public String getLocation() { return location; }
    public int getMaxBorrowDays() { return maxBorrowDays; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return available; }
    public Student getOwner() { return owner; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setConditionLabel(String conditionLabel) { this.conditionLabel = conditionLabel; }
    public void setLocation(String location) { this.location = location; }
    public void setMaxBorrowDays(int maxBorrowDays) { this.maxBorrowDays = maxBorrowDays; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setOwner(Student owner) { this.owner = owner; }
}
