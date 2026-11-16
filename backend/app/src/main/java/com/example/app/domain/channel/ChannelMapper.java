package com.example.app.domain.channel;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ChannelMapper {

    // SELECT: 1件取得
    @Select("""
        SELECT *
        FROM channels
        WHERE id = #{id}
        AND status != 2
    """)
    Optional<ChannelEntity> findById(Long id);


    // SELECT: 1件取得
    @Select("""
        SELECT *
        FROM channels
        WHERE name = #{name}
        AND status != 2
    """)
    Optional<ChannelEntity> findByName(String name);


    // SELECT: 全件取得
    @Select("""
        SELECT *
        FROM channels
        WHERE status != 2
        ORDER BY id ASC
    """)
    List<ChannelEntity> findAll();


    // INSERT: 新規作成
    @Insert("""
        INSERT INTO channels(name, status)
        VALUES(#{name}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChannelEntity channel);


    // UPDATE: name + status を編集
    @Update("""
        UPDATE channels
        SET name = #{name},
            status = #{status}
        WHERE id = #{id}
    """)
    void update(ChannelEntity channel);


    // UPDATE: status のみ変更（アーカイブ化・アクティブ化・削除）
    @Update("""
        UPDATE channels
        SET status = #{status}
        WHERE id = #{id}
    """)
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);


    // DELETE: 物理削除（管理者用）
    @Delete("""
        DELETE FROM channels
        WHERE id = #{id}
    """)
    void deleteById(Long id);

    @Select("""
        SELECT COUNT(*)
        FROM channels
        WHERE name = #{name}
    """)
    boolean existsByName(String name);

    @Select("""
        SELECT COUNT(*)
        FROM channels
        WHERE name = #{name}
        AND id != #{id}
    """)
    boolean existsByNameExceptId(Long id, String name);

}
