package com.example.app.dto.message;

import java.util.List;

import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.dto.mention.MentionResponseDTO;
import com.example.app.dto.sync.ReactionSummaryDTO;

public record MessageSummaryDTO (
        MessageDTO message,
        List<ReactionSummaryDTO> reactions,
        List<MentionResponseDTO> mentions,
        List<AttachmentDTO> attachments
) {}

