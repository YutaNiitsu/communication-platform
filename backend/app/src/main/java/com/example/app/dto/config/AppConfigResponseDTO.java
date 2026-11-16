package com.example.app.dto.config;

import java.util.List;

public record AppConfigResponseDTO(
    Message message,
    Attachment attachment,
    Storage storage
) {

    public record Message(
        int maxLength,
        int pageRange
    ) {
    }

    public record Attachment(
        List<String> allowedExtensions,
        int maxFileCount,
        long maxFileSizeMb,
        long maxTotalSizeMb
    ) {
    }

    public record Storage(
        int warnThresholdPercent,
        int deleteThresholdPercent
    ) {
    }
}