package com.example.app.domain.messageTemplate;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

@Mapper
public interface MessageTemplateMapper {

    // SELECT: 1件取得
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE id = #{id}
    """)
    Optional<MessageTemplateEntity> findById(Long id);


    // SELECT: user_id
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByUserId(Long userId);


    // SELECT: is_shared
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE is_shared = #{isShared}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByIsShared(Boolean isShared);


    // SELECT: category_id
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE category_id = #{categoryId}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByCategoryId(Long categoryId);


    // SELECT: user_id AND category_id
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE user_id = #{userId}
          AND category_id = #{categoryId}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByUserIdAndCategoryId(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId
    );


    // SELECT: user_id AND is_shared
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE user_id = #{userId}
          AND is_shared = #{isShared}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByUserIdAndIsShared(
            @Param("userId") Long userId,
            @Param("isShared") Boolean isShared
    );


    // SELECT: category_id AND is_shared
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE category_id = #{categoryId}
          AND is_shared = #{isShared}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByCategoryIdAndIsShared(
            @Param("categoryId") Long categoryId,
            @Param("isShared") Boolean isShared
    );


    // SELECT: user_id AND category_id AND is_shared
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            title, 
            content, 
            category_id AS categoryId,
            is_shared AS isShared, 
            created_at AS createdAt, 
            updated_at AS updatedAt
        FROM message_templates
        WHERE user_id = #{userId}
          AND category_id = #{categoryId}
          AND is_shared = #{isShared}
        ORDER BY created_at DESC
    """)
    List<MessageTemplateEntity> findByUserIdAndCategoryIdAndIsShared(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("isShared") Boolean isShared
    );


    // 名前の存在チェック（重複防止）
    @Select("""
        SELECT COUNT(*)
        FROM message_templates
        WHERE title = #{name}
    """)
    boolean existsByName(String name);


    // INSERT
    @Insert("""
        INSERT INTO message_templates(user_id, title, content,
                                      category_id, is_shared,
                                      created_at, updated_at)
        VALUES(#{userId}, #{title}, #{content},
               #{categoryId}, #{isShared},
               NOW(), NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MessageTemplateEntity template);


    // UPDATE
    @Update("""
        UPDATE message_templates
        SET title = #{title},
            content = #{content},
            category_id = #{categoryId},
            is_shared = #{isShared},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void update(MessageTemplateEntity template);


    // DELETE（物理削除）
    @Delete("""
        DELETE FROM message_templates
        WHERE id = #{id}
    """)
    void deleteById(Long id);
}
