package com.shadoww.library.dto;

import jakarta.validation.constraints.*;

public record MemberRequestDto(
        @NotBlank(message = "Name is required")
        String name
) {}
