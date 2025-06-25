package com.shadoww.library.dto;


import jakarta.validation.constraints.NotNull;

public record BorrowRequestDto(
        @NotNull(message = "Member ID is required")
        Long memberId,

        @NotNull(message = "Book ID is required")
        Long bookId
) {}