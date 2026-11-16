package com.example.app.domain.user;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    // SELECT: 全件取得
    @Select("""
        SELECT 
            id,
            username,
            password_hash AS passwordHash,
            role,
            is_active AS isActive,
            created_at AS createdAt
        FROM users
    """)
    List<UserEntity> findAll();

    // SELECT: 1件取得（id）
    @Select("""
        SELECT 
            id,
            username,
            password_hash AS passwordHash,
            role,
            is_active AS isActive,
            created_at AS createdAt
        FROM users
        WHERE id = #{id}
    """)
    Optional<UserEntity> findById(Long id);


    // SELECT: username
    @Select("""
        SELECT 
            id,
            username,
            password_hash AS passwordHash,
            role,
            is_active AS isActive,
            created_at AS createdAt
        FROM users
        WHERE username = #{username}
    """)
    Optional<UserEntity> findByUsername(String username);


    // SELECT: username の存在チェック
    @Select("""
        SELECT COUNT(*)
        FROM users
        WHERE username = #{username}
    """)
    boolean existsByUsername(String username);


    // SELECT: username 前方一致検索
    @Select("""
        SELECT 
            id,
            username,
            password_hash AS passwordHash,
            role,
            is_active AS isActive,
            created_at AS createdAt
        FROM users
        WHERE username LIKE CONCAT(#{prefix}, '%')
        ORDER BY username ASC
    """)
    List<UserEntity> findByUsernameStartingWith(String prefix);


    // INSERT: 新規作成
    @Insert("""
        INSERT INTO users(username, password_hash, role, is_active, created_at)
        VALUES(#{username}, #{passwordHash}, #{role}, #{isActive}, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserEntity user);


    // UPDATE: 編集
    @Update("""
        UPDATE users
        SET username = #{username},
            password_hash = #{passwordHash},
            role = #{role},
            is_active = #{isActive}
        WHERE id = #{id}
    """)
    void update(UserEntity user);


    // DELETE: 物理削除（管理者用）
    @Delete("""
        DELETE FROM users
        WHERE id = #{id}
    """)
    void deleteById(Long id);
}
