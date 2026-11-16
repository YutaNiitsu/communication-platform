package com.example.app.dto.messageTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageTemplateEditRequestDTO(
        String title,
        String content,
        @JsonProperty("category_id")
        Long categoryId,
        @JsonProperty("is_shared")
        boolean isShared
) {}