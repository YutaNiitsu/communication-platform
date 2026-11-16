package com.example.app.domain.messageTemplate;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

@Mapper
public interface MessageTemplateCategoryMapper {

    // SELECT: 1件取得
    @Select("""
        SELECT
            id, 
            user_id AS userId, 
            name, 
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_template_categories
        WHERE id = #{id}
    """)
     Optional<MessageTemplateCategoryEntity> findById(Long id);


    // SELECT: userId で取得（本人のカテゴリ一覧）
    @Select("""
        SELECT
            id, 
            user_id AS userId, 
            name, 
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_template_categories
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateCategoryEntity> findByUserId(Long userId);


    // SELECT: isShared で取得（共有カテゴリ一覧）
    @Select("""
        SELECT
            id, 
            user_id AS userId, 
            name, 
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_template_categories
        WHERE is_shared = #{isShared}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateCategoryEntity> findByIsShared(Boolean isShared);


    // SELECT: userId + isShared（本人の共有カテゴリ or 非共有カテゴリ）
    @Select("""
        SELECT
            id, 
            user_id AS userId, 
            name, 
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_template_categories
        WHERE user_id = #{userId}
          AND is_shared = #{isShared}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateCategoryEntity> findByUserIdAndIsShared(
            @Param("userId") Long userId,
            @Param("isShared") Boolean isShared
    );


    // SELECT: 名前の存在チェック（重複防止）
    @Select("""
        SELECT COUNT(*)
        FROM message_template_categories
        WHERE name = #{name}
    """)
    boolean existsByName(String name);


    // INSERT: 新規作成
    @Insert("""
        INSERT INTO message_template_categories(user_id, name, is_shared,
                                                created_at, updated_at)
        VALUES(#{userId}, #{name}, #{isShared}, NOW(), NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MessageTemplateCategoryEntity category);


    // UPDATE: 編集
    @Update("""
        UPDATE message_template_categories
        SET name = #{name},
            is_shared = #{isShared},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void update(MessageTemplateCategoryEntity category);


    // DELETE: 物理削除
    @Delete("""
        DELETE FROM message_template_categories
        WHERE id = #{id}
    """)
    void deleteById(Long id);
}
