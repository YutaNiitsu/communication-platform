package com.example.app.dto.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StorageDeleteResponseDTO (
    @JsonProperty("deleted_count")
    int deletedCount,
    @JsonProperty("usage_percent_after")
    int usagePercentAfter
) {}
