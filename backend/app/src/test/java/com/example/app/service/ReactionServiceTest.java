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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock JwtProvider jwtProvider;
    @Mock UserMapper userMapper;
    @Mock OperationLogService operationLogService;
    @Mock ReactionMapper reactionMapper;
    @Mock MessageMapper messageMapper;
    @Mock WebSocketEventPublisher eventPublisher;
    @Mock AuthUtil authUtil;
    @Mock HttpServletRequest httpReq;

    ReactionService service;

    @BeforeEach
    void setup() {
        service = new ReactionService(
                jwtProvider,
                userMapper,
                operationLogService,
                reactionMapper,
                messageMapper,
                eventPublisher,
                authUtil
        );

        lenient().when(jwtProvider.extractTokenFromCookie(httpReq)).thenReturn("token");
    }

    @Test
    @DisplayName("1-1: addReaction - 新規リアクション追加成功")
    void testAddReaction_normal() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageEntity msg = new MessageEntity();
        msg.setId(100L);
        msg.setChannelId(1L);
        when(messageMapper.findById(100L)).thenReturn(Optional.of(msg));

        when(reactionMapper.findByMessageIdAndUserIdAndReactionType(100L, 10L, "👍"))
                .thenReturn(Optional.empty());

        // insert 時に ID を付与するだけ
        doAnswer(inv -> {
            ReactionEntity r = inv.getArgument(0);
            r.setId(500L);
            return null;
        }).when(reactionMapper).insert(any());

        ReactionResponseDTO res = service.addReaction(100L, "👍", httpReq);

        assertThat(res.added()).isTrue();
        assertThat(res.reactionType()).isEqualTo("👍");

        // ★ createdAt は LocalDateTime.now() なので any(LocalDateTime.class) を使う
        verify(eventPublisher).sendReactionAdded(
                eq(1L),        // channelId
                eq(100L),      // messageId
                eq(10L),       // userId
                eq("👍"),      // reactionType
                any(LocalDateTime.class)  // createdAt
        );
    }


    @Test
    @DisplayName("1-2: addReaction - 既存リアクション検出")
    void testAddReaction_existing() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageEntity msg = new MessageEntity();
        msg.setId(100L);
        msg.setChannelId(1L);
        when(messageMapper.findById(100L)).thenReturn(Optional.of(msg));

        ReactionEntity existing = new ReactionEntity();
        existing.setId(500L);
        existing.setMessageId(100L);
        existing.setUserId(10L);
        existing.setReactionType("👍");

        when(reactionMapper.findByMessageIdAndUserIdAndReactionType(100L, 10L, "👍"))
                .thenReturn(Optional.of(existing));

        ReactionResponseDTO res = service.addReaction(100L, "👍", httpReq);

        assertThat(res.added()).isFalse();
        assertThat(res.message()).isEqualTo("Reaction already exists");
    }

    @Test
    @DisplayName("1-3: addReaction - messageId 不存在")
    void testAddReaction_messageNotFound() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        when(messageMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addReaction(999L, "👍", httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Message not found");
    }

    @Test
    @DisplayName("1-4: addReaction - reactionType=null")
    void testAddReaction_reactionTypeNull() {
        assertThatThrownBy(() -> service.addReaction(100L, null, httpReq))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("2-1: removeReaction - 正常削除成功")
    void testRemoveReaction_normal() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageEntity msg = new MessageEntity();
        msg.setId(100L);
        msg.setChannelId(1L);
        when(messageMapper.findById(100L)).thenReturn(Optional.of(msg));

        ReactionEntity reaction = new ReactionEntity();
        reaction.setMessageId(100L);
        reaction.setUserId(10L);
        reaction.setReactionType("👍");

        when(reactionMapper.findByMessageIdAndUserIdAndReactionType(100L, 10L, "👍"))
                .thenReturn(Optional.of(reaction));

        service.removeReaction(100L, "👍", httpReq);

        verify(reactionMapper).deleteByMessageIdAndUserIdAndReactionType(100L, 10L, "👍");
        verify(eventPublisher).sendReactionRemoved(1L, 100L, 10L, "👍");
    }

    @Test
    @DisplayName("2-2: removeReaction - messageId 不存在")
    void testRemoveReaction_messageNotFound() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        when(messageMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeReaction(999L, "👍", httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Message not found");
    }

    @Test
    @DisplayName("2-3: removeReaction - reaction 不存在")
    void testRemoveReaction_reactionNotFound() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageEntity msg = new MessageEntity();
        msg.setId(100L);
        msg.setChannelId(1L);
        when(messageMapper.findById(100L)).thenReturn(Optional.of(msg));

        when(reactionMapper.findByMessageIdAndUserIdAndReactionType(100L, 10L, "👍"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeReaction(100L, "👍", httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("reaction not found");
    }

    @Test
    @DisplayName("2-4: removeReaction - reactionType=null")
    void testRemoveReaction_reactionTypeNull() {

        assertThatThrownBy(() -> service.removeReaction(100L, null, httpReq))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("reactionType is null");
    }

    @Test
    @DisplayName("3-1: getReactions - 正常集計")
    void testGetReactions_normal() {

        MessageEntity msg = new MessageEntity();
        msg.setId(100L);
        when(messageMapper.findById(100L)).thenReturn(Optional.of(msg));

        ReactionEntity r1 = new ReactionEntity();
        r1.setReactionType("👍");
        r1.setUserId(10L);

        ReactionEntity r2 = new ReactionEntity();
        r2.setReactionType("👍");
        r2.setUserId(20L);

        ReactionEntity r3 = new ReactionEntity();
        r3.setReactionType("❤️");
        r3.setUserId(30L);

        when(reactionMapper.findByMessageId(100L))
                .thenReturn(List.of(r1, r2, r3));

        List<Map<String, Object>> res = service.getReactions(100L);

        assertThat(res).hasSize(2);

        Map<String, Object> thumbs = res.stream()
                .filter(m -> m.get("reactionType").equals("👍"))
                .findFirst().get();

        assertThat(thumbs.get("count")).isEqualTo(2);
        assertThat((List<Long>) thumbs.get("users")).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    @DisplayName("3-2: getReactions - messageId 不存在")
    void testGetReactions_messageNotFound() {

        when(messageMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReactions(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Message not found");
    }



}
