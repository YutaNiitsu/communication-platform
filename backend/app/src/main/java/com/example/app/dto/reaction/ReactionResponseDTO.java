package com.example.app.dto.reaction;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReactionResponseDTO(
        Long id,
        @JsonProperty("message_id")
        Long messageId,
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("reaction_type")
        String reactionType,
        boolean added,
        String message
) {}
