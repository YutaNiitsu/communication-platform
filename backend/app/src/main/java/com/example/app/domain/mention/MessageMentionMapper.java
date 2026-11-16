package com.example.app.domain.mention;

import java.util.List;

import org.apache.ibatis.annotations.*;

@Mapper
public interface MessageMentionMapper {

    // SELECT: 1件取得（id）
    @Select("""
        SELECT 
            id, 
            message_id AS messageId, 
            mentioned_user_id AS mentionedUserId, 
            created_at AS createdAt
        FROM message_mentions
        WHERE id = #{id}
    """)
    MessageMentionEntity findById(Long id);


    // SELECT: messageId で取得（メッセージ内の全メンション）
    @Select("""
        SELECT 
            id, 
            message_id AS messageId, 
            mentioned_user_id AS mentionedUserId, 
            created_at AS createdAt
        FROM message_mentions
        WHERE message_id = #{messageId}
        ORDER BY id ASC
    """)
    List<MessageMentionEntity> findByMessageId(Long messageId);


    // SELECT: mentionedUserId で取得（ユーザーがメンションされた全メッセージ）
    @Select("""
        SELECT 
            id, 
            message_id AS messageId, 
            mentioned_user_id AS mentionedUserId, 
            created_at AS createdAt
        FROM message_mentions
        WHERE mentioned_user_id = #{mentionedUserId}
        ORDER BY id ASC
    """)
    List<MessageMentionEntity> findByMentionedUserId(Long mentionedUserId);


    // SELECT: messageId + mentionedUserId（ユニーク制約）
    @Select("""
        SELECT 
            id, 
            message_id AS messageId, 
            mentioned_user_id AS mentionedUserId, 
            created_at AS createdAt
        FROM message_mentions
        WHERE message_id = #{messageId}
          AND mentioned_user_id = #{mentionedUserId}
    """)
    MessageMentionEntity findByMessageIdAndMentionedUserId(
            @Param("messageId") Long messageId,
            @Param("mentionedUserId") Long mentionedUserId
    );


    // INSERT: メンション追加
    @Insert("""
        INSERT INTO message_mentions(message_id, mentioned_user_id)
        VALUES(#{messageId}, #{mentionedUserId})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MessageMentionEntity mention);


    // DELETE: メッセージ内の特定ユーザーのメンション削除
    @Delete("""
        DELETE FROM message_mentions
        WHERE message_id = #{messageId}
          AND mentioned_user_id = #{mentionedUserId}
    """)
    void deleteByMessageIdAndMentionedUserId(
            @Param("messageId") Long messageId,
            @Param("mentionedUserId") Long mentionedUserId
    );


    // DELETE: メッセージ内の全メンション削除（メッセージ削除時）
    @Delete("""
        DELETE FROM message_mentions
        WHERE message_id = #{messageId}
    """)
    void deleteByMessageId(Long messageId);


    // DELETE: ユーザーがメンションされた履歴を全削除（ユーザー削除時）
    @Delete("""
        DELETE FROM message_mentions
        WHERE mentioned_user_id = #{mentionedUserId}
    """)
    void deleteByMentionedUserId(Long mentionedUserId);
}
