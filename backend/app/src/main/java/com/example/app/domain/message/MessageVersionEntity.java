package com.example.app.domain.message;
// package com.example.app.domain.message;

// import jakarta.persistence.*;
// import java.time.LocalDateTime;

// @Entity
// @Table(name = "message_versions")
// public class MessageVersionEntity {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     // FK → messages.id
//     
//     @JoinColumn(name = "message_id", nullable = false)
//     private MessageEntity message;

//     @Column(nullable = false, columnDefinition = "TEXT")
//     private String beforeContent;

//     @Column(nullable = false, columnDefinition = "TEXT")
//     private String afterContent;

//     @Column(nullable = false)
//     private LocalDateTime editedAt = LocalDateTime.now();

//     // --- Getter / Setter ---
//     public Long getId() { return id; }
//     public void setId(Long id) { this.id = id; }

//     public MessageEntity getMessage() { return message; }
//     public void setMessage(MessageEntity message) { this.message = message; }

//     public String getBeforeContent() { return beforeContent; }
//     public void setBeforeContent(String beforeContent) { this.beforeContent = beforeContent; }

//     public String getAfterContent() { return afterContent; }
//     public void setAfterContent(String afterContent) { this.afterContent = afterContent; }

//     public LocalDateTime getEditedAt() { return editedAt; }
//     public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
// }
