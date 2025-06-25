package com.shadoww.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookRequestDto(
        @NotBlank(message = "Title is required")
        @Size(min = 3, message = "Title must be at least 3 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Pattern(
                regexp = "^[A-Z][a-z]+\\s[A-Z][a-z]+$",
                message = "Author must be in format 'Name Surname'"
        )
        String author

) {}