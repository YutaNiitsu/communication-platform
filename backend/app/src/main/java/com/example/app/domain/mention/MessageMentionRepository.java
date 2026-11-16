// package com.example.app.domain.mention;

// import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.List;

// public interface MessageMentionRepository extends JpaRepository<MessageMentionEntity, Long> {

//     // メッセージに紐づくメンション一覧
//     List<MessageMentionEntity> findByMessageId(Long messageId);

//     // メンションされたユーザーの一覧
//     List<MessageMentionEntity> findByMentionedUserId(Long userId);

//     // メッセージ + ユーザーで検索
//     List<MessageMentionEntity> findByMessageIdAndMentionedUserId(Long messageId, Long userId);
// }
