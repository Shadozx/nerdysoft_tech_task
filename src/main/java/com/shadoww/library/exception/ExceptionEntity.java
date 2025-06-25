package com.shadoww.library.exception;

import java.time.LocalDateTime;

public record ExceptionEntity(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
}
