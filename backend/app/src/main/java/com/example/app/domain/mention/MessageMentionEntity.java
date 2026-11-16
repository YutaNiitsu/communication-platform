package com.example.app.domain.mention;

import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_mentions")
public class MessageMentionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → messages.id
    @JoinColumn(name = "message_id", nullable = false)
    private Long messageId;

    // FK → users.id（メンションされたユーザー）
    
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private Long mentionedUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Getter / Setter ---
    public Long getId() { return id; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getMentionedUserId() { return mentionedUserId; }
    public void setMentionedUserId(Long mentionedUserId) { this.mentionedUserId = mentionedUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
