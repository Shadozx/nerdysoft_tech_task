package com.shadoww.library.dto;

import java.time.LocalDateTime;

public record MemberResponseDto(
        Long id,
        String name,
        LocalDateTime membershipDate
) {}