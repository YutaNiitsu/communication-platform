package com.example.app.websocket;

import com.example.app.dto.sync.SyncRequestDTO;
import com.example.app.service.JwtProvider;
import com.example.app.service.SyncService;
import com.example.app.websocket.session.UserSessionRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketEventPublisher eventPublisher;
    private final SyncService syncService;
    private final UserSessionRegistry sessionRegistry;
    private final JwtProvider jwtProvider;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // ------------------------------
    // 接続管理
    // ------------------------------

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = jwtProvider.extractTokenFromCookie(session);
        Long userId = jwtProvider.getUserIdFromToken(token);
        sessionRegistry.register(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String token = jwtProvider.extractTokenFromCookie(session);
        Long userId = jwtProvider.getUserIdFromToken(token);
        sessionRegistry.unregister(userId, session); // ★ session を渡す
    }

    // ------------------------------
    // メッセージ受信
    // ------------------------------

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        Map<String, Object> json = objectMapper.readValue(
                message.getPayload(),
                new TypeReference<Map<String, Object>>() {}
        );

        String action = (String) json.get("action");
        String token = jwtProvider.extractTokenFromCookie(session);
        Long userId = jwtProvider.getUserIdFromToken(token);

        switch (action) {
            case "subscribe" -> handleSubscribe(userId, json);
            case "unsubscribe" -> handleUnsubscribe(userId, json);
            case "ping" -> handlePing(userId, json);
            case "sync" -> handleSync(userId, json);
            default -> {
                // 未定義アクション
                appLog.warn("Unknown action: {}", action);
                throw new IllegalArgumentException("Unknown action: " + action);
            }
        }
    }

    // ------------------------------
    // Action: subscribe
    // ------------------------------

    private void handleSubscribe(Long userId, Map<String, Object> json) {
        Long channelId = ((Number) json.get("channel_id")).longValue();
        sessionRegistry.subscribe(userId, channelId);
    }

    // ------------------------------
    // Action: unsubscribe
    // ------------------------------

    private void handleUnsubscribe(Long userId, Map<String, Object> json) {
        Long channelId = ((Number) json.get("channel_id")).longValue();
        sessionRegistry.unsubscribe(userId, channelId);
    }

    // ------------------------------
    // Action: ping
    // ------------------------------

    private void handlePing(Long userId, Map<String, Object> json) {
        long ts = ((Number) json.get("timestamp")).longValue();
        eventPublisher.sendPong(userId, ts);
    }

    // ------------------------------
    // Action: sync
    // ------------------------------

    private void handleSync(Long userId, Map<String, Object> json) {

        List<Long> channelIds = objectMapper.convertValue(
                json.get("channelIds"),
                new TypeReference<List<Long>>() {}
        );

        Long lastMessageId = ((Number) json.get("lastMessageId")).longValue();

        SyncRequestDTO req = new SyncRequestDTO(channelIds, lastMessageId);

        syncService.sync(userId, req);
    }
}
