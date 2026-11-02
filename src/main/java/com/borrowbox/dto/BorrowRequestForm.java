package com.borrowbox.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BorrowRequestForm(
        @NotNull Long studentId,
        @NotNull Long itemId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate expectedReturnDate,
        @Size(max = 1000) String message) {}
