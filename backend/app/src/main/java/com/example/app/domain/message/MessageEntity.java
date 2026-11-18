package com.example.app.domain.message;

import com.example.app.domain.channel.ChannelEntity;
import com.example.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → channels.id
    
    @JoinColumn(name = "channel_id", nullable = false)
    private Long channelId;

    // FK → users.id
    
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    // FK → messages.id (返信先メッセージ)
    
    @JoinColumn(name = "reply_to_message_id")
    @Column(nullable = true)
    private Long replyToMessageId;

    @JoinColumn(name = "request_id")
    @Column(nullable = false, unique = true)
    private String requestId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int status = 0; // 0: アクティブ, 1: アーカイブ, 2: 削除

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { 
        this.status = status;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getReplyToMessageId() {
        return replyToMessageId;
    }
    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
