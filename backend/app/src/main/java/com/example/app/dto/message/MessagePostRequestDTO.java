package com.example.app.dto.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessagePostRequestDTO(
        String content,
        @JsonProperty("reply_to_message_id")
        Long replyToMessageId,
        @JsonProperty("mentioned_user_id")
        List<Long> mentionedUserId,
        @JsonProperty("request_id")
        String requestId
) {}