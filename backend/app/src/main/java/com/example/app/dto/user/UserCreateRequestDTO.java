package com.example.app.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserCreateRequestDTO(
        String username,
        String password,
        int role,
        @JsonProperty("is_active")
        boolean isActive
) {}
