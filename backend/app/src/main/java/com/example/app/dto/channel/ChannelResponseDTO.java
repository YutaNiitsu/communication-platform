package com.example.app.dto.channel;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChannelResponseDTO(
        Long id,
        String name,
        int status,
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {}