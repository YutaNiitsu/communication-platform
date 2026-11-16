package com.example.app.dto.message;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDeleteResponseDTO(
        Long id,
        boolean deleted,
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}
