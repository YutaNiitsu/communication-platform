package com.example.app.domain.operationLog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/operation_logs.sql"})
class OperationLogMapperTest {

    @Autowired
    private OperationLogMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で 1件取得成功")
    void testFindById_normal() {
        OperationLogEntity result = mapper.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getOperationType()).isEqualTo(OperationType.message_created);
    }

    @Test
    @DisplayName("1-2: createdAt が正しく取得される")
    void testFindById_createdAt() {
        OperationLogEntity result = mapper.findById(1L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は null")
    void testFindById_notFound() {
        OperationLogEntity result = mapper.findById(999L);
        assertThat(result).isNull();
    }


    // 2. findByUserOrderByCreatedAtDesc
    @Test
    @DisplayName("2-1: ユーザー別ログ取得成功")
    void testFindByUserOrderByCreatedAtDesc_normal() {
        List<OperationLogEntity> results = mapper.findByUserOrderByCreatedAtDesc(1L);
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("2-2: createdAt 降順で取得")
    void testFindByUserOrderByCreatedAtDesc_order() {
        List<OperationLogEntity> results = mapper.findByUserOrderByCreatedAtDesc(1L);
        assertThat(results.get(0).getCreatedAt()).isAfter(results.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("2-3: 該当なしは空リスト")
    void testFindByUserOrderByCreatedAtDesc_empty() {
        List<OperationLogEntity> results = mapper.findByUserOrderByCreatedAtDesc(999L);
        assertThat(results).isEmpty();
    }


    // 3. findByOperationTypeOrderByCreatedAtDesc
    @Test
    @DisplayName("3-1: 操作種別ログ取得成功")
    void testFindByOperationTypeOrderByCreatedAtDesc_normal() {
        List<OperationLogEntity> results = mapper.findByOperationTypeOrderByCreatedAtDesc("message_created");
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("3-2: createdAt 降順で取得")
    void testFindByOperationTypeOrderByCreatedAtDesc_order() {
        List<OperationLogEntity> results = mapper.findByOperationTypeOrderByCreatedAtDesc("message_created");
        assertThat(results.get(0).getCreatedAt()).isAfter(results.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("3-3: 該当なしは空リスト")
    void testFindByOperationTypeOrderByCreatedAtDesc_empty() {
        List<OperationLogEntity> results = mapper.findByOperationTypeOrderByCreatedAtDesc("unknown");
        assertThat(results).isEmpty();
    }


    // 4. findByUserAndOperationTypeOrderByCreatedAtDesc
    @Test
    @DisplayName("4-1: ユーザー＋操作種別で取得成功")
    void testFindByUserAndOperationTypeOrderByCreatedAtDesc_normal() {
        List<OperationLogEntity> results = mapper.findByUserAndOperationTypeOrderByCreatedAtDesc(1L, "message_updated");
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("4-2: createdAt 降順で取得")
    void testFindByUserAndOperationTypeOrderByCreatedAtDesc_order() {
        List<OperationLogEntity> results = mapper.findByUserAndOperationTypeOrderByCreatedAtDesc(1L, "message_created");
        assertThat(results.get(0).getCreatedAt()).isAfter(results.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("4-3: 該当なしは空リスト")
    void testFindByUserAndOperationTypeOrderByCreatedAtDesc_empty() {
        List<OperationLogEntity> results = mapper.findByUserAndOperationTypeOrderByCreatedAtDesc(1L, "unknown");
        assertThat(results).isEmpty();
    }


    // 5. insert
    @Test
    @DisplayName("5-1: ログ追加成功")
    void testInsert_normal() {
        OperationLogEntity log = new OperationLogEntity();
        log.setUserId(1L);
        log.setOperationType(OperationType.message_deleted);
        log.setTarget("msgX");
        log.setResult("ok");

        mapper.insert(log);

        assertThat(log.getId()).isNotNull();

        OperationLogEntity saved = mapper.findById(log.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getOperationType()).isEqualTo(OperationType.message_deleted);
    }


    // 6. deleteById
    @Test
    @DisplayName("6-1: 削除成功")
    void testDeleteById_normal() {
        mapper.deleteById(1L);
        assertThat(mapper.findById(1L)).isNull();
    }

    @Test
    @DisplayName("6-2: 存在しない id の削除は件数 0")
    void testDeleteById_notFound() {
        mapper.deleteById(999L);
        assertThat(mapper.findById(999L)).isNull();
    }
}
