package com.example.app.dto.messageTemplate;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageTemplateResponseDTO(
        Long id,
        @JsonProperty("user_id")
        Long userId,
        String title,
        String content,
        @JsonProperty("category_id")
        Long categoryId,
        @JsonProperty("is_shared")
        boolean isShared,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}