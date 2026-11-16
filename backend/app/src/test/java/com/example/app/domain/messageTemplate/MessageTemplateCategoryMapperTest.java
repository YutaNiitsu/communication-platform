package com.example.app.domain.messageTemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/message_template_categories.sql"})
class MessageTemplateCategoryMapperTest {

    @Autowired
    private MessageTemplateCategoryMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で 1件取得成功")
    void testFindById_normal() {
        Optional<MessageTemplateCategoryEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test");
    }

    @Test
    @DisplayName("1-2: createdAt / updatedAt が正しく取得される")
    void testFindById_dates() {
        Optional<MessageTemplateCategoryEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は empty")
    void testFindById_notFound() {
        Optional<MessageTemplateCategoryEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 2. findByUserId
    @Test
    @DisplayName("2-1: ユーザーのカテゴリ一覧取得")
    void testFindByUserId_normal() {
        List<MessageTemplateCategoryEntity> results = mapper.findByUserId(1L);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("2-2: 該当なしは空リスト")
    void testFindByUserId_empty() {
        List<MessageTemplateCategoryEntity> results = mapper.findByUserId(999L);
        assertThat(results).isEmpty();
    }


    // 3. findByIsShared
    @Test
    @DisplayName("3-1: 共有カテゴリ一覧取得")
    void testFindByIsShared_true() {
        List<MessageTemplateCategoryEntity> results = mapper.findByIsShared(true);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("3-2: 非共有カテゴリ一覧取得")
    void testFindByIsShared_false() {
        List<MessageTemplateCategoryEntity> results = mapper.findByIsShared(false);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("3-3: 該当なしは空リスト")
    @Sql({"/testdata/schema.sql", "/testdata/message_template_categories_empty_shared.sql"})
    void testFindByIsShared_empty() {
        List<MessageTemplateCategoryEntity> results = mapper.findByIsShared(true);
        assertThat(results).isEmpty();
    }


    // 4. findByUserIdAndIsShared
    @Test
    @DisplayName("4-1: 本人の共有カテゴリ取得")
    void testFindByUserIdAndIsShared_shared() {
        List<MessageTemplateCategoryEntity> results = mapper.findByUserIdAndIsShared(1L, true);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("4-2: 本人の非共有カテゴリ取得")
    void testFindByUserIdAndIsShared_private() {
        List<MessageTemplateCategoryEntity> results = mapper.findByUserIdAndIsShared(1L, false);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("4-3: 該当なしは空リスト")
    void testFindByUserIdAndIsShared_empty() {
        List<MessageTemplateCategoryEntity> results = mapper.findByUserIdAndIsShared(999L, true);
        assertThat(results).isEmpty();
    }


    // 5. existsByName
    @Test
    @DisplayName("5-1: 名前が存在する場合 TRUE")
    void testExistsByName_true() {
        boolean exists = mapper.existsByName("test");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("5-2: 名前が存在しない場合 FALSE")
    void testExistsByName_false() {
        boolean exists = mapper.existsByName("unknown");
        assertThat(exists).isFalse();
    }


    // 6. insert
    @Test
    @DisplayName("6-1: 新規作成成功")
    void testInsert_normal() {
        MessageTemplateCategoryEntity category = new MessageTemplateCategoryEntity();
        category.setUserId(1L);
        category.setName("new_category");
        category.setShared(true);

        mapper.insert(category);

        assertThat(category.getId()).isNotNull();

        Optional<MessageTemplateCategoryEntity> saved = mapper.findById(category.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("new_category");
    }


    // 7. update
    @Test
    @DisplayName("7-1: 編集成功")
    void testUpdate_normal() {
        MessageTemplateCategoryEntity category = mapper.findById(1L).get();
        category.setName("updated_name");
        category.setShared(false);

        mapper.update(category);

        MessageTemplateCategoryEntity updated = mapper.findById(1L).get();
        assertThat(updated.getName()).isEqualTo("updated_name");
        assertThat(updated.isShared()).isFalse();
    }

    @Test
    @DisplayName("7-2: 存在しない id の更新は件数 0")
    void testUpdate_notFound() {
        MessageTemplateCategoryEntity category = new MessageTemplateCategoryEntity();
        category.setId(999L);
        category.setName("x");
        category.setShared(true);

        mapper.update(category);

        assertThat(mapper.findById(999L)).isEmpty();
    }


    // 8. deleteById
    @Test
    @DisplayName("8-1: 削除成功")
    void testDeleteById_normal() {
        mapper.deleteById(1L);
        assertThat(mapper.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("8-2: 存在しない id の削除は件数 0")
    void testDeleteById_notFound() {
        mapper.deleteById(999L);
        assertThat(mapper.findById(999L)).isEmpty();
    }
}
