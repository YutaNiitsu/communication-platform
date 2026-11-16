package com.example.app.dto.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StorageUsageResponseDTO (
    @JsonProperty("usage_percent")
    int usagePercent
){}
