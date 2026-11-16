package com.example.app.service;

import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.reaction.ReactionEntity;
import com.example.app.domain.reaction.ReactionMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.reaction.ReactionResponseDTO;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;
    private final ReactionMapper reactionMapper;
    private final MessageMapper messageMapper;
    private final WebSocketEventPublisher eventPublisher;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // --- POST /messages/{id}/reactions ---
    public ReactionResponseDTO addReaction(Long messageId, String reactionType, HttpServletRequest req) {
        if (reactionType == null ) {
            appLog.warn("reactionType is null");
            throw new NullPointerException("reactionType is null");
        }
        String token = jwtProvider.extractTokenFromCookie(req);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageEntity message = messageMapper.findById(messageId)
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        // 既に押しているか確認
        ReactionEntity existing = reactionMapper
                .findByMessageIdAndUserIdAndReactionType(messageId, requestUser.getId(), reactionType)
                .orElse(null);

        if (existing != null) {
            // 既に存在 → added=false
            return new ReactionResponseDTO(
                    existing.getId(),
                    messageId,
                    requestUser.getId(),
                    reactionType,
                    false,
                    "Reaction already exists"
            );
        }

        ReactionEntity reaction = new ReactionEntity();
        reaction.setMessageId(message.getId());
        reaction.setUserId(requestUser.getId());
        reaction.setReactionType(reactionType);

        reactionMapper.insert(reaction);

        // WebSocket 通知
        eventPublisher.sendReactionAdded(
            message.getChannelId(), 
            messageId,
            requestUser.getId(),
            reactionType,
            reaction.getCreatedAt()
        );

        // 操作ログ
        operationLogService.record(
                requestUser,
                OperationType.reaction_added,
                List.of("message_id=" + messageId + ", user_id=" + requestUser.getId()),
                List.of("reaction_type=" + reactionType)
        );

        return new ReactionResponseDTO(
                reaction.getId(),
                messageId,
                requestUser.getId(),
                reactionType,
                true,
                null
        );
    }

    // --- DELETE /messages/{id}/reactions ---
    public void removeReaction(Long messageId, String reactionType, HttpServletRequest req) {
        if (reactionType == null ) {
            appLog.warn("reactionType is null");
            throw new NullPointerException("reactionType is null");
        }

        String token = jwtProvider.extractTokenFromCookie(req);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageEntity message = messageMapper.findById(messageId)
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        ReactionEntity reaction = reactionMapper
                .findByMessageIdAndUserIdAndReactionType(messageId, requestUser.getId(), reactionType)
                .orElseGet(() -> {
                    appLog.warn("Reaction not found (message_id={}, user_id={}, reaction_type={})", messageId, requestUser.getId(), reactionType);
                    throw new NotFoundException("reaction not found");
                });

        reactionMapper.deleteByMessageIdAndUserIdAndReactionType(messageId, requestUser.getId(), reactionType);

        // WebSocket 通知
        eventPublisher.sendReactionRemoved(
            message.getChannelId(), 
            messageId,
            requestUser.getId(),
            reactionType
        );

        // 操作ログ
        operationLogService.record(
            requestUser,
            OperationType.reaction_removed,
            List.of("message_id=" + messageId + ", user_id=" + requestUser.getId()),
            List.of("reaction_type=" + reactionType)
        );
    }

    // --- GET /messages/{id}/reactions ---
    public List<Map<String, Object>> getReactions(Long messageId) {

        messageMapper.findById(messageId)
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        List<ReactionEntity> reactions = reactionMapper.findByMessageId(messageId);

        // 絵文字ごとに集計
        Map<String, List<ReactionEntity>> grouped =
                reactions.stream().collect(Collectors.groupingBy(ReactionEntity::getReactionType));

        List<Map<String, Object>> result = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            String reactionType = entry.getKey();
            List<ReactionEntity> list = entry.getValue();

            result.add(Map.of(
                    "reactionType", reactionType,
                    "count", list.size(),
                    "users", list.stream().map(r -> r.getUserId()).toList()
            ));
        }

        return result;
    }
}
