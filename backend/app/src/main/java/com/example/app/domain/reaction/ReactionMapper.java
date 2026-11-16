package com.example.app.domain.reaction;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ReactionMapper {

    // SELECT: 1件取得（id）
    @Select("""
        SELECT
            id, 
            message_id AS messageId, 
            user_id AS userId, 
            reaction_type AS reactionType, 
            created_at AS createdAt
        FROM message_reactions
        WHERE id = #{id}
    """)
    ReactionEntity findById(Long id);


    // SELECT: messageId + userId + reactionType（ユニーク制約）
    @Select("""
        SELECT
            id, 
            message_id AS messageId, 
            user_id AS userId, 
            reaction_type AS reactionType, 
            created_at AS createdAt
        FROM message_reactions
        WHERE message_id = #{messageId}
          AND user_id = #{userId}
          AND reaction_type = #{reactionType}
    """)
    Optional<ReactionEntity> findByMessageIdAndUserIdAndReactionType(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId,
            @Param("reactionType") String reactionType
    );


    // SELECT: メッセージのリアクション一覧
    @Select("""
        SELECT
            id, 
            message_id AS messageId, 
            user_id AS userId, 
            reaction_type AS reactionType, 
            created_at AS createdAt
        FROM message_reactions
        WHERE message_id = #{messageId}
        ORDER BY created_at ASC
    """)
    List<ReactionEntity> findByMessageId(Long messageId);


    // INSERT: リアクション追加
    @Insert("""
        INSERT INTO message_reactions(message_id, user_id, reaction_type, created_at)
        VALUES(#{messageId}, #{userId}, #{reactionType}, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ReactionEntity reaction);


    // DELETE: 特定リアクション削除
    @Delete("""
        DELETE FROM message_reactions
        WHERE message_id = #{messageId}
          AND user_id = #{userId}
          AND reaction_type = #{reactionType}
    """)
    void deleteByMessageIdAndUserIdAndReactionType(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId,
            @Param("reactionType") String reactionType
    );


    // DELETE: メッセージ内の全リアクション削除（メッセージ削除時）
    @Delete("""
        DELETE FROM message_reactions
        WHERE message_id = #{messageId}
    """)
    void deleteByMessageId(Long messageId);


    // DELETE: ユーザーのリアクション履歴削除（ユーザー削除時）
    @Delete("""
        DELETE FROM message_reactions
        WHERE user_id = #{userId}
    """)
    void deleteByUserId(Long userId);
}
