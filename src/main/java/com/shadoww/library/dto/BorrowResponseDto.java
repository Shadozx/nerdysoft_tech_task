package com.shadoww.library.dto;

import java.time.LocalDateTime;

public record BorrowResponseDto(
        Long id,
        Long bookId,
        Long memberId,

        LocalDateTime borrowDate,
        LocalDateTime returnDate,

        boolean returned
) {
}
