package com.borrowbox.dto;

import com.borrowbox.model.Item;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String name,
        String category,
        String description,
        String condition,
        String location,
        int maxBorrowDays,
        String imageUrl,
        boolean available,
        Long ownerId,
        String ownerName,
        String department,
        LocalDateTime createdAt) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getCategory(), item.getDescription(),
                item.getConditionLabel(), item.getLocation(), item.getMaxBorrowDays(), item.getImageUrl(),
                item.isAvailable(), item.getOwner().getId(), item.getOwner().getName(),
                item.getOwner().getDepartment(), item.getCreatedAt());
    }
}
