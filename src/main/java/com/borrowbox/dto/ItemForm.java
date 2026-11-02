package com.borrowbox.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemForm(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 80) String conditionLabel,
        @NotBlank @Size(max = 120) String location,
        @Min(1) @Max(60) int maxBorrowDays,
        @Size(max = 1000) String imageUrl,
        @NotNull Long ownerId) {}
