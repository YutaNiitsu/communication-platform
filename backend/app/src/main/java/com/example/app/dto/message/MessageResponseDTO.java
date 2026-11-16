package com.example.app.dto.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageResponseDTO(
    List<MessageSummaryDTO> messages,
    @JsonProperty("has_more") boolean hasMore
) {
}
