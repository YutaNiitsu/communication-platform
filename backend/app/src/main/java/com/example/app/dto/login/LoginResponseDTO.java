package com.example.app.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {
    @JsonProperty("user_id")
    private Long userId;
    private String username;
    private int role;
    @JsonProperty("is_active")
    private boolean isActive;
}
