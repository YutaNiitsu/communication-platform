package com.example.app.websocket;

import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.dto.message.MessageSummaryDTO;
import com.example.app.websocket.session.UserSessionRegistry;
import com.example.app.dto.sync.SyncResultMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.jsonwebtoken.io.IOException;

import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WebSocketEventPublisher {

    private final UserSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public WebSocketEventPublisher(UserSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ------------------------------
    // 共通送信メソッド
    // ------------------------------
    // ------------------------------
    // 1. sendToUser（ユーザーの全セッションへ送信）
    // ------------------------------
    private void sendToUser(Long userId, Object payload) {

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            // JSON変換エラー → 処理中断
            appLog.error("[WebSocket] JSON serialization error", e);
            notifyAdmin("JSON serialization error: " + e.getMessage());
            return;
        }

        for (WebSocketSession session : sessionRegistry.getSessions(userId)) {
            try {
                if (session == null) {
                    appLog.info("[WebSocket] session is null (userId={})", userId);
                    continue; // 処理継続
                }

                if (!session.isOpen()) {
                    appLog.info("[WebSocket] session is closed (userId={})", userId);
                    continue; // 処理継続
                }

                session.sendMessage(new TextMessage(json));

            } catch (MessageDeliveryException e) {
                // 送信するデータ量が多すぎる → 処理中断
                appLog.error("[WebSocket] MessageDeliveryException", e);
                notifyAdmin("MessageDeliveryException: " + e.getMessage());
                return;

            } catch (IOException e) {
                // クライアント切断 → 処理継続
                appLog.info("[WebSocket] IOException (client disconnected)", e);
                continue;

            } catch (OutOfMemoryError e) {
                // 致命的 → 処理中断
                appLog.error("[WebSocket] OutOfMemoryError", e);
                notifyAdmin("OutOfMemoryError: " + e.getMessage());
                return;

            } catch (Exception e) {
                // その他予期しない例外 → 処理継続（ログは ERROR）
                appLog.error("[WebSocket] Unexpected error", e);
                continue;
            }
        }
    }

    // ------------------------------
    // 2. sendToChannel（チャンネル購読者の全セッションへ送信）
    // ------------------------------
    private void sendToChannel(Long channelId, Object payload) {

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            appLog.error("[WebSocket] JSON serialization error", e);
            notifyAdmin("JSON serialization error: " + e.getMessage());
            return; // 処理中断
        }

        for (Long userId : sessionRegistry.getUsersSubscribedTo(channelId)) {
            for (WebSocketSession session : sessionRegistry.getSessions(userId)) {
                try {
                    if (session == null) {
                        appLog.info("[WebSocket] session is null (userId={})", userId);
                        continue;
                    }

                    if (!session.isOpen()) {
                        appLog.info("[WebSocket] session is closed (userId={})", userId);
                        continue;
                    }

                    session.sendMessage(new TextMessage(json));

                } catch (MessageDeliveryException e) {
                    appLog.error("[WebSocket] MessageDeliveryException", e);
                    notifyAdmin("MessageDeliveryException: " + e.getMessage());
                    return; // 処理中断

                } catch (IOException e) {
                    appLog.info("[WebSocket] IOException (client disconnected)", e);
                    continue;

                } catch (OutOfMemoryError e) {
                    appLog.error("[WebSocket] OutOfMemoryError", e);
                    notifyAdmin("OutOfMemoryError: " + e.getMessage());
                    return; // 処理中断

                } catch (Exception e) {
                    appLog.error("[WebSocket] Unexpected error", e);
                    continue;
                }
            }
        }
    }

    // ------------------------------
    // メッセージイベント
    // ------------------------------

    public void sendMessageCreated(Long channelId, Map<String, Object> message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "message_created");
        payload.put("channel_id", channelId);
        payload.put("message", message);

        sendToChannel(channelId, payload);
    }

    public void sendMessageUpdated(Long channelId, Map<String, Object> message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "message_updated");
        payload.put("channel_id", channelId);
        payload.put("message", message);

        sendToChannel(channelId, payload);
    }

    public void sendMessageDeleted(Long channelId, Map<String, Object> message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "message_deleted");
        payload.put("channel_id", channelId);
        payload.put("message", message);

        sendToChannel(channelId, payload);
    }

    public void sendMentionAdded(Long userId, Long messageId, Map<String, Object> mention) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "mention_created");
        payload.put("message_id", messageId);
        payload.put("mention", mention);

        sendToUser(userId, payload);
    }

    public void sendReactionAdded(Long channelId, Long messageId, Long userId, String reactionType, LocalDateTime createdAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "reaction_added");
        payload.put("message_id", messageId);
        payload.put("user_id", userId);
        payload.put("reaction_type", reactionType);
        payload.put("created_at", createdAt);

        sendToChannel(channelId, payload);
    }

    public void sendReactionRemoved(Long channelId, Long messageId, Long userId, String reactionType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "reaction_removed");
        payload.put("message_id", messageId);
        payload.put("user_id", userId);
        payload.put("reaction_type", reactionType);

        sendToChannel(channelId, payload);
    }

    // ------------------------------
    // チャンネルイベント
    // ------------------------------
    public void sendChannelCreated(Long channelId, Map<String, Object> channel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "channel_created");
        payload.put("channel_id", channelId);
        payload.put("channel", channel);

        sendToChannel(channelId, payload);
    }

    public void sendChannelUpdated(Long channelId, Map<String, Object> channel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "channel_updated");
        payload.put("channel_id", channelId);
        payload.put("channel", channel);

        sendToChannel(channelId, payload);
    }

    public void sendChannelJoined(Long channelId, Map<String, Object> userInfo) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "channel_joined");
        payload.put("channel_id", channelId);
        payload.put("user", userInfo);

        sendToChannel(channelId, payload);
    }

    public void sendChannelLeft(Long channelId, Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "channel_left");
        payload.put("channel_id", channelId);
        payload.put("user_id", userId);

        sendToChannel(channelId, payload);
    }

    public void sendChannelDeleted(Long channelId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "channel_deleted");
        payload.put("channel_id", channelId);

        sendToChannel(channelId, payload);
    }

    // ------------------------------
    // pong（接続確認）
    // ------------------------------

    public void sendPong(Long userId, long timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "pong");
        payload.put("timestamp", timestamp);

        sendToUser(userId, payload);
    }

    // ------------------------------
    // sync_result（再接続時の同期イベント）
    // ------------------------------

    public void sendSyncResult(Long userId, List<MessageSummaryDTO> messages) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "sync_result");
        payload.put("messages", messages);

        sendToUser(userId, payload);
    }

    // ------------------------------
    // 管理者通知（メール・Slack・監視システムなど） 通知方法は未定
    // ------------------------------
    private void notifyAdmin(String message) {
        // TODO: Slack / Email / PagerDuty などに通知
        appLog.error("[ADMIN NOTIFY] {}", message);
    }

    // ------------------------------
    // 添付ファイルイベント
    // ------------------------------
    public void publishAttachmentAdded(Long channelId, AttachmentDTO attachment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "attachment_added");
        payload.put("channel_id", channelId);
        payload.put("attachment", attachment);

        sendToChannel(channelId, payload);
    }

    public void publishAttachmentRemoved(Long channelId, Long messageId, Long attachmentId, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "attachment_removed");
        payload.put("channel_id", channelId);
        payload.put("message_id", messageId);
        payload.put("attachment_id", attachmentId);
        payload.put("reason", reason);

        sendToChannel(channelId, payload);
    }
}
