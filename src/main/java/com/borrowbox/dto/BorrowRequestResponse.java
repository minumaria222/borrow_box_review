package com.borrowbox.dto;

import com.borrowbox.model.BorrowRequest;
import com.borrowbox.model.RequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowRequestResponse(
        Long id,
        Long studentId,
        String studentName,
        String studentDepartment,
        Long itemId,
        String itemName,
        String itemImageUrl,
        Long ownerId,
        String ownerName,
        LocalDate startDate,
        LocalDate expectedReturnDate,
        String message,
        RequestStatus status,
        LocalDateTime requestedAt) {
    public static BorrowRequestResponse from(BorrowRequest request) {
        return new BorrowRequestResponse(request.getId(), request.getStudent().getId(), request.getStudent().getName(),
                request.getStudent().getDepartment(), request.getItem().getId(), request.getItem().getName(),
                request.getItem().getImageUrl(), request.getItem().getOwner().getId(), request.getItem().getOwner().getName(),
                request.getStartDate(), request.getExpectedReturnDate(), request.getMessage(), request.getStatus(),
                request.getRequestedAt());
    }
}
