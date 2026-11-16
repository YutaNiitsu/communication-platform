// package com.example.app.domain.reaction;

// import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.List;
// import java.util.Optional;

// public interface ReactionRepository extends JpaRepository<ReactionEntity, Long> {

//     // 1ユーザー1リアクション（同じ絵文字）
//     Optional<ReactionEntity> findByMessageIdAndUserIdAndReactionType(Long messageId, Long userId, String reactionType);

//     // メッセージのリアクション一覧
//     List<ReactionEntity> findByMessageId(Long messageId);
// }
