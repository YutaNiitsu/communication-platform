package com.example.app.dto.attachment;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttachmentDTO (
    Long id,
    @JsonProperty("message_id")
    Long messageId,
    @JsonProperty("original_file_name")
    String originalFileName,
    @JsonProperty("stored_file_name")
    String storedFileName,
    @JsonProperty("file_url")
    String fileUrl,
    @JsonProperty("thumbnail_url")
    String thumbnailUrl,
    @JsonProperty("file_type")
    String fileType,
    @JsonProperty("file_size")
    long fileSize,
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {}
