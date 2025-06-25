package com.shadoww.library.dto;

import java.time.LocalDateTime;

public record BorrowedBookDto(
        String bookTitle,
        String bookAuthor,
        LocalDateTime borrowDate,
        LocalDateTime returnDate,
        boolean returned
) {}