package com.example.app.dto.search;

import java.time.LocalDate;

public record SearchRequestDTO(
        String keyword,
        Long channelId,
        Long userId,
        LocalDate from,
        LocalDate to
) {}