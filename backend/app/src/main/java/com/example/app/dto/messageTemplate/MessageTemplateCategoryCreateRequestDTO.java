package com.example.app.dto.messageTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageTemplateCategoryCreateRequestDTO(
        String name,
        @JsonProperty("is_shared")
        boolean isShared
) {}
