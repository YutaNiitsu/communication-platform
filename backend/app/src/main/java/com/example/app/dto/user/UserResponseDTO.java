package com.example.app.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponseDTO (
    Long userId,
    String username,
    int role,
    boolean isActive
) {}
