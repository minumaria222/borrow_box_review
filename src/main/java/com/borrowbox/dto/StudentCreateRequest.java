package com.borrowbox.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentCreateRequest(
        @NotBlank String name,
        @NotBlank String department,
        @NotBlank @Email String email) {}
