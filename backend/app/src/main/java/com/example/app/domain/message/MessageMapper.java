package com.example.app.domain.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

import com.example.app.dto.message.MessageDTO;

@Mapper
public interface MessageMapper {
    // SELECT: 全件取得
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE status != 2
    """)
    List<MessageEntity> findAll();

    // SELECT: 1件取得
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE id = #{id}
    """)
    Optional<MessageEntity> findById(Long id);


    // SELECT: チャンネル＋ステータス
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE channel_id = #{channelId}
          AND status = #{status}
        ORDER BY id ASC
    """)
    List<MessageEntity> findByChannelAndStatus(
            @Param("channelId") Long channelId,
            @Param("status") Integer status
    );


    // SELECT: ユーザー＋ステータス
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE user_id = #{userId}
          AND status = #{status}
        ORDER BY id ASC
    """)
    List<MessageEntity> findByUserAndStatus(
            @Param("userId") Long userId,
            @Param("status") Integer status
    );


    // SELECT: チャンネル内の全メッセージ
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE channel_id = #{channelId}
          AND status != 2
        ORDER BY id ASC
    """)
    List<MessageEntity> findByChannel(Long channelId);


    // SELECT: ユーザーの全メッセージ
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE user_id = #{userId}
          AND status != 2
        ORDER BY id ASC
    """)
    List<MessageEntity> findByUser(Long userId);


    // SELECT: lastMessageId より新しいメッセージ
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE channel_id = #{channelId}
          AND id > #{lastMessageId}
          AND status != 2
        ORDER BY id ASC
    """)
    List<MessageEntity> findByChannelIdAndIdGreaterThan(
            @Param("channelId") Long channelId,
            @Param("lastMessageId") Long lastMessageId
    );


    // SELECT: 返信元メッセージのスレッド取得
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE reply_to_message_id = #{replyToMessageId}
        AND status != 2
        ORDER BY id ASC
    """)
    List<MessageEntity> findByReplyToMessageId(Long replyToMessageId);


    // SELECT: messageId より前のメッセージ（range 付き）
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE channel_id = #{channelId}
          AND id <= #{messageId}
          AND status != 2
        ORDER BY id DESC
        LIMIT #{range}
    """)
    List<MessageEntity> findPreviousMessages(
            @Param("channelId") Long channelId,
            @Param("messageId") Long messageId,
            @Param("range") Integer range
    );


    // SELECT: 最新メッセージ（range 付き）
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE channel_id = #{channelId}
          AND status != 2
        ORDER BY id DESC
        LIMIT #{range}
    """)
    List<MessageEntity> findLatestMessages(
            @Param("channelId") Long channelId,
            @Param("range") Integer range
    );


    // SELECT: messageId より前のメッセージすべて
    @Select("""
        SELECT
            id,
            channel_id AS channelId,
            user_id AS userId,
            reply_to_message_id AS replyToMessageId,
            content,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM messages
        WHERE channel_id = #{channelId}
          AND id <= #{messageId}
          AND status != 2
        ORDER BY id DESC
    """)
    List<MessageEntity> findAllPreviousMessages(
            @Param("channelId") Long channelId,
            @Param("messageId") Long messageId
    );


    // INSERT
    @Insert("""
        INSERT INTO messages(channel_id, user_id, reply_to_message_id,
                             content, status, created_at, updated_at)
        VALUES(#{channelId}, #{userId}, #{replyToMessageId},
               #{content}, #{status}, NOW(), NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MessageEntity message);


    // UPDATE（編集）
    @Update("""
        UPDATE messages
        SET content = #{content},
            status = #{status},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void update(MessageEntity message);


    // DELETE（論理削除 → status=2）
    @Update("""
        UPDATE messages
        SET status = 2,
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void logicalDelete(Long id);


    // 検索（MyBatis 動的 SQL）
    List<MessageDTO> searchMessages(
        String keyword,
        Long channelId,
        Long userId,
        LocalDateTime from,
        LocalDateTime to
    );

    @Select("""
    SELECT CASE WHEN EXISTS (
        SELECT 1
        FROM messages
        WHERE channel_id = #{channelId}
          AND id < #{messageId}
          AND status != 2
    ) THEN TRUE ELSE FALSE END
""")
boolean existsPreviousMessages(
        @Param("channelId") Long channelId,
        @Param("messageId") Long messageId
);


    
}
