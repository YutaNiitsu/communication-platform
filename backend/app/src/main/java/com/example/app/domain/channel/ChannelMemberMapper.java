package com.example.app.domain.channel;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ChannelMemberMapper {

    // SELECT: 1件取得（id）
    @Select("""
        SELECT 
            id, 
            channel_id AS channelId, 
            user_id AS userId, 
            joined_at AS joinedAt
        FROM channel_members
        WHERE id = #{id}
    """)
    Optional<ChannelMemberEntity> findById(Long id);


    // SELECT: チャンネル＋ユーザー（ユニーク制約）
    @Select("""
        SELECT 
            id, 
            channel_id AS channelId, 
            user_id AS userId, 
            joined_at AS joinedAt
        FROM channel_members
        WHERE channel_id = #{channelId}
          AND user_id = #{userId}
    """)
    Optional<ChannelMemberEntity> findByChannelAndUser(
            @Param("channelId") Long channelId,
            @Param("userId") Long userId
    );


    // SELECT: チャンネルに所属する全ユーザー
    @Select("""
        SELECT 
            id, 
            channel_id AS channelId, 
            user_id AS userId, 
            joined_at AS joinedAt
        FROM channel_members
        WHERE channel_id = #{channelId}
        ORDER BY joined_at ASC
    """)
    List<ChannelMemberEntity> findByChannel(Long channelId);


    // SELECT: ユーザーが所属する全チャンネル
    @Select("""
        SELECT 
            id, 
            channel_id AS channelId, 
            user_id AS userId, 
            joined_at AS joinedAt
        FROM channel_members
        WHERE user_id = #{userId}
        ORDER BY joined_at ASC
    """)
    List<ChannelMemberEntity> findByUser(Long userId);


    // INSERT: チャンネル参加
    @Insert("""
        INSERT INTO channel_members(channel_id, user_id)
        VALUES(#{channelId}, #{userId})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChannelMemberEntity member);


    // DELETE: チャンネル退出（1ユーザー）
    @Delete("""
        DELETE FROM channel_members
        WHERE channel_id = #{channelId}
          AND user_id = #{userId}
    """)
    void deleteByChannelAndUser(
            @Param("channelId") Long channelId,
            @Param("userId") Long userId
    );


    // DELETE: チャンネル内の全メンバー削除（チャンネル削除時）
    @Delete("""
        DELETE FROM channel_members
        WHERE channel_id = #{channelId}
    """)
    void deleteByChannel(Long channelId);


    // DELETE: ユーザーが所属する全チャンネル削除（ユーザー削除時）
    @Delete("""
        DELETE FROM channel_members
        WHERE user_id = #{userId}
    """)
    void deleteByUser(Long userId);
}
