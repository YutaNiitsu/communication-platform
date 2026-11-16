package com.example.app.websocket.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class UserSessionRegistry {
    // ユーザーIDをキーに、複数のWebSocketSession（複数タブ・複数ブラウザ対応）を保持する
    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    
    // ユーザーIDをキーに購読チャンネルIDのSetを管理
    private final Map<Long, Set<Long>> subscriptions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        subscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
    }

    public void unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(session);
            // すべてのタブ・ウィンドウが閉じられた場合のみセッションマップと購読情報をクリア
            if (userSessions.isEmpty()) {
                sessions.remove(userId);
                subscriptions.remove(userId);
            }
        }
    }

    public void subscribe(Long userId, Long channelId) {
        subscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(channelId);
    }

    public void unsubscribe(Long userId, Long channelId) {
        Set<Long> channels = subscriptions.get(userId);
        if (channels != null) {
            channels.remove(channelId);
        }
    }

    // 単一セッションを返すメソッド（互換性維持のため最初に見つかったものを返すか、必要に応じて使用）
    public WebSocketSession getSession(Long userId) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions != null && !userSessions.isEmpty()) {
            return userSessions.iterator().next();
        }
        return null;
    }

    // ユーザーの全セッションを取得するメソッド（複数送信用）
    public Set<WebSocketSession> getSessions(Long userId) {
        return sessions.getOrDefault(userId, Set.of());
    }

    public Set<Long> getSubscribedChannels(Long userId) {
        return subscriptions.getOrDefault(userId, Set.of());
    }

    public Set<Long> getUsersSubscribedTo(Long channelId) {
        Set<Long> result = new HashSet<>();
        for (Map.Entry<Long, Set<Long>> entry : subscriptions.entrySet()) {
            Long userId = entry.getKey();
            Set<Long> channels = entry.getValue();
            if (channels != null && channels.contains(channelId)) {
                result.add(userId);
            }
        }
        return result;
    }
}