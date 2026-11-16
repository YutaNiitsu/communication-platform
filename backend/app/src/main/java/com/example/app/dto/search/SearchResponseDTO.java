package com.example.app.dto.search;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchResponseDTO(
        Long id,
        @JsonProperty("channel_id")
        Long channelId,
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("reply_to_message_id")
        Long replyToMessageId,
        String content,
        int status,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}
