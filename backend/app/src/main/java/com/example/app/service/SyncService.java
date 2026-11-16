package com.example.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.domain.attachment.AttachmentMapper;
import com.example.app.domain.mention.MessageMentionEntity;
import com.example.app.domain.mention.MessageMentionMapper;
import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.reaction.ReactionEntity;
import com.example.app.domain.reaction.ReactionMapper;
import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.dto.mention.MentionResponseDTO;
import com.example.app.dto.message.MessageDTO;
import com.example.app.dto.message.MessageSummaryDTO;
import com.example.app.dto.sync.*;
import com.example.app.websocket.WebSocketEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final MessageMapper messageMapper;
    private final ReactionMapper reactionMapper;
    private final MessageMentionMapper mentionMapper;
    private final AttachmentMapper attachmentMapper;
    private final WebSocketEventPublisher eventPublisher;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public void sync(Long userId, SyncRequestDTO req) {

        // --- null チェック ---
        if (req == null) {

            appLog.warn("SyncRequestDTO is required");
            throw new IllegalArgumentException("SyncRequestDTO is required");
        }
        if (req.channelIds() == null || req.channelIds().isEmpty()) {
            appLog.warn("channelIds is required");
            throw new IllegalArgumentException("channelIds is required");
        }
        if (req.lastMessageId() == null) {
            appLog.warn("lastMessageId is required");
            throw new IllegalArgumentException("lastMessageId is required");
        }

        List<MessageSummaryDTO> results = new ArrayList<>();

        try {
            for (Long channelId : req.channelIds()) {

                List<MessageEntity> messages =
                        messageMapper.findByChannelIdAndIdGreaterThan(channelId, req.lastMessageId());

                for (MessageEntity m : messages) {

                    // --- message DTO ---
                    MessageDTO messageDTO = new MessageDTO(
                            m.getId(),
                            m.getChannelId(),
                            m.getUserId(),
                            m.getReplyToMessageId(),
                            m.getContent(),
                            m.getStatus(),
                            m.getCreatedAt(),
                            m.getUpdatedAt()
                    );

                    // --- reactions ---
                    List<ReactionEntity> reactions = reactionMapper.findByMessageId(m.getId());
                    Map<String, List<ReactionEntity>> grouped =
                            reactions.stream().collect(Collectors.groupingBy(ReactionEntity::getReactionType));

                    List<ReactionSummaryDTO> reactionDTOs = grouped.entrySet().stream()
                            .map(e -> new ReactionSummaryDTO(
                                    e.getKey(),
                                    e.getValue().size(),
                                    e.getValue().stream().map(r -> r.getUserId()).toList()
                            ))
                            .toList();

                    // --- mentions ---
                    List<MessageMentionEntity> mentions = mentionMapper.findByMessageId(m.getId());
                    List<MentionResponseDTO> mentionDTOs = mentions.stream()
                            .map(mm -> new MentionResponseDTO(
                                    mm.getId(),
                                    mm.getMentionedUserId(),
                                    mm.getCreatedAt()
                            ))
                            .toList();
                    
                    // --- attachments ---
                    List<AttachmentEntity> attachments = attachmentMapper.findByMessageId(m.getId());
                    List<AttachmentDTO> attachmentDTOs = attachments.stream()
                            .map(a -> new AttachmentDTO(
                                    a.getId(),
                                    a.getMessageId(),
                                    a.getOriginalFileName(),
                                    a.getStoredFileName(),
                                    a.getFileUrl(),
                                    a.getThumbnailUrl(),
                                    a.getFileType(),
                                    a.getFileSize(),
                                    a.getCreatedAt()
                            ))
                            .toList();

                    // --- 結合 ---
                    results.add(new MessageSummaryDTO(
                            messageDTO,
                            reactionDTOs,
                            mentionDTOs,
                            attachmentDTOs
                    ));
                }
            }

            // --- WebSocket 送信 ---
            eventPublisher.sendSyncResult(userId, results);

        } catch (Exception e) {
            appLog.error("Failed to sync messages: " + e.getMessage());
        }
    }
}
