package com.example.app.dto.sync;

import java.util.List;

import com.example.app.dto.mention.MentionResponseDTO;
import com.example.app.dto.message.MessageDTO;

public record SyncResultMessageDTO(
        MessageDTO message,
        List<ReactionSummaryDTO> reactions,
        List<MentionResponseDTO> mentions
) {}
