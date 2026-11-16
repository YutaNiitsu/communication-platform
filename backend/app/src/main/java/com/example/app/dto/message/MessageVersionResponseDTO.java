package com.example.app.dto.message;

import java.time.LocalDateTime;

public record MessageVersionResponseDTO(
        Long id,
        Long messageId,
        String beforeContent,
        String afterContent,
        LocalDateTime editedAt
) {}