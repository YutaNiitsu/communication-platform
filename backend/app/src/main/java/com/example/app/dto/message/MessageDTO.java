package com.example.app.dto.message;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDTO(
        Long id,
        @JsonProperty("channel_id")
        Long channelId,
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("reply_to_message_id")
        Long replyToMessageId,
        String content,
        int status, // 0: アクティブ, 1: アーカイブ, 2: 削除
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}