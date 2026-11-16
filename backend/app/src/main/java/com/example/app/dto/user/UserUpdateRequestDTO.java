package com.example.app.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserUpdateRequestDTO(
        String username,
        String password,
        int role,
        @JsonProperty("is_active")
        Boolean isActive
) {}
