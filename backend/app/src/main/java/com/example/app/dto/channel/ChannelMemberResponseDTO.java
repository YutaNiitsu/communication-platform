package com.example.app.dto.channel;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChannelMemberResponseDTO(
	@JsonProperty("user_id")
	Long userId,
	String username,
	int role,
	@JsonProperty("is_active")
	boolean isActive,
	@JsonProperty("joined_at")
	LocalDateTime joinedAt
) {}