package com.example.app.domain.operationLog;

import java.util.List;

import org.apache.ibatis.annotations.*;

@Mapper
public interface OperationLogMapper {

    // SELECT: 1件取得
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            operation_type AS operationType, 
            target, 
            result, 
            created_at AS createdAt
        FROM operation_logs
        WHERE id = #{id}
    """)
    OperationLogEntity findById(Long id);


    // SELECT: ユーザー別ログ一覧（降順）
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            operation_type AS operationType, 
            target, 
            result, 
            created_at AS createdAt
        FROM operation_logs
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
    """)
    List<OperationLogEntity> findByUserOrderByCreatedAtDesc(Long userId);


    // SELECT: 操作種別別ログ一覧（降順）
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            operation_type AS operationType, 
            target, 
            result, 
            created_at AS createdAt
        FROM operation_logs
        WHERE operation_type = #{operationType}
        ORDER BY created_at DESC
    """)
    List<OperationLogEntity> findByOperationTypeOrderByCreatedAtDesc(String operationType);


    // SELECT: ユーザー＋操作種別（降順）
    @Select("""
        SELECT 
            id, 
            user_id AS userId, 
            operation_type AS operationType, 
            target, 
            result, 
            created_at AS createdAt
        FROM operation_logs
        WHERE user_id = #{userId}
          AND operation_type = #{operationType}
        ORDER BY created_at DESC
    """)
    List<OperationLogEntity> findByUserAndOperationTypeOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("operationType") String operationType
    );


    // INSERT: ログ追加
    @Insert("""
        INSERT INTO operation_logs(user_id, operation_type, target, result, created_at)
        VALUES(#{userId}, #{operationType}, #{target}, #{result}, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OperationLogEntity log);


    // DELETE: 物理削除（管理者用）
    @Delete("""
        DELETE FROM operation_logs
        WHERE id = #{id}
    """)
    void deleteById(Long id);
}
