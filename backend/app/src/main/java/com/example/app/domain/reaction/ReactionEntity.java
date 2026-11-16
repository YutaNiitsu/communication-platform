package com.example.app.domain.reaction;

import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "message_reactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_id", "user_id", "reaction_type"})
    }
)
public class ReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → messages.id
    
    @JoinColumn(name = "message_id", nullable = false)
    private Long messageId;

    // FK → users.id
    
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    // 絵文字
    @Column(name = "reaction_type", nullable = false, length = 50)
    private String reactionType;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
