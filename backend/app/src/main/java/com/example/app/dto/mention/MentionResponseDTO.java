package com.example.app.dto.mention;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MentionResponseDTO (
    Long id,
    @JsonProperty("mentioned_user_id")
    Long mentionedUserId,
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {}
