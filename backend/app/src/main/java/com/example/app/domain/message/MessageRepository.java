// package com.example.app.domain.message;

// import com.example.app.domain.channel.ChannelEntity;
// import com.example.app.domain.user.UserEntity;
// import org.springframework.data.jpa.repository.JpaRepository;

// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.List;

// public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

//     // チャンネル内のメッセージ一覧
//     List<MessageEntity> findByChannelAndStatus(ChannelEntity channel, int status);

//     // ユーザーが投稿したメッセージ一覧
//     List<MessageEntity> findByUserAndStatus(UserEntity user, int status);

//     // チャンネル内の全メッセージ（削除済み含む）
//     List<MessageEntity> findByChannel(ChannelEntity channel);

//     // ユーザーの全メッセージ（削除済み含む）
//     List<MessageEntity> findByUser(UserEntity user);

//     List<MessageEntity> findByChannelIdAndIdGreaterThan(Long channelId, Long lastMessageId);

//     List<MessageEntity> findByReplyToMessageId(Long replyToMessageId);

//     @Query("""
//         SELECT m
//         FROM MessageEntity m
//         LEFT JOIN FETCH m.channel
//         LEFT JOIN FETCH m.user
//         WHERE m.channel.id = :channelId
//           AND m.id <= :messageId
//           AND m.status != 2
//         ORDER BY m.id DESC
//         """)
//     List<MessageEntity> findPreviousMessages(
//             @Param("channelId") Long channelId,
//             @Param("messageId") Long messageId,
//             Pageable pageable
//     );

//     @Query("""
//         SELECT m
//         FROM MessageEntity m
//         LEFT JOIN FETCH m.channel
//         LEFT JOIN FETCH m.user
//         WHERE m.channel.id = :channelId
//         AND m.status != 2
//         ORDER BY m.id DESC
//         """)
//     List<MessageEntity> findLatestMessages(
//             @Param("channelId") Long channelId,
//             Pageable pageable
//     );

//     @Query("""
//         SELECT m
//         FROM MessageEntity m
//         LEFT JOIN FETCH m.channel
//         LEFT JOIN FETCH m.user
//         WHERE m.channel.id = :channelId
//         AND m.id <= :messageId
//         AND m.status != 2
//         ORDER BY m.id DESC
//         """)
//     List<MessageEntity> findAllPreviousMessages(
//             @Param("channelId") Long channelId,
//             @Param("messageId") Long messageId
//     );

//     @Query("""
//         SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END
//         FROM MessageEntity m
//         WHERE m.channel.id = :channelId
//         AND m.id < :messageId
//         AND m.status <> 2
//     """)
//     boolean existsPreviousMessages(
//             @Param("channelId") Long channelId,
//             @Param("messageId") Long messageId
//     );

// }