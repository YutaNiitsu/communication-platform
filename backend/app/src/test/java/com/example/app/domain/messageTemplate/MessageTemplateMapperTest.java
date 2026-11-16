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
@Sql({"/testdata/schema.sql", "/testdata/message_templates.sql"})
class MessageTemplateMapperTest {

    @Autowired
    private MessageTemplateMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で 1件取得成功")
    void testFindById_normal() {
        Optional<MessageTemplateEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("test");
    }

    @Test
    @DisplayName("1-2: 日付が正しく取得される")
    void testFindById_dates() {
        Optional<MessageTemplateEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は empty")
    void testFindById_notFound() {
        Optional<MessageTemplateEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 2. findByUserId
    @Test
    @DisplayName("2-1: ユーザーのテンプレート一覧取得")
    void testFindByUserId_normal() {
        List<MessageTemplateEntity> results = mapper.findByUserId(1L);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("2-2: 該当なしは空リスト")
    void testFindByUserId_empty() {
        List<MessageTemplateEntity> results = mapper.findByUserId(999L);
        assertThat(results).isEmpty();
    }


    // 3. findByIsShared
    @Test
    @DisplayName("3-1: 共有テンプレート取得")
    void testFindByIsShared_true() {
        List<MessageTemplateEntity> results = mapper.findByIsShared(true);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("3-2: 非共有テンプレート取得")
    void testFindByIsShared_false() {
        List<MessageTemplateEntity> results = mapper.findByIsShared(false);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("3-3: 該当なしは空リスト")
    @Sql({"/testdata/schema.sql", "/testdata/message_templates_empty_shared.sql"})
    void testFindByIsShared_empty() {
        List<MessageTemplateEntity> results = mapper.findByIsShared(true);
        assertThat(results).isEmpty();
    }


    // 4. findByCategoryId
    @Test
    @DisplayName("4-1: カテゴリ内テンプレート取得")
    void testFindByCategoryId_normal() {
        List<MessageTemplateEntity> results = mapper.findByCategoryId(1L);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("4-2: 該当なし")
    void testFindByCategoryId_empty() {
        List<MessageTemplateEntity> results = mapper.findByCategoryId(999L);
        assertThat(results).isEmpty();
    }


    // 5. findByUserIdAndCategoryId
    @Test
    @DisplayName("5-1: 本人のカテゴリ内テンプレート取得")
    void testFindByUserIdAndCategoryId_normal() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndCategoryId(1L, 1L);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("5-2: 該当なし")
    void testFindByUserIdAndCategoryId_empty() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndCategoryId(1L, 999L);
        assertThat(results).isEmpty();
    }


    // 6. findByUserIdAndIsShared
    @Test
    @DisplayName("6-1: 本人の共有テンプレート取得")
    void testFindByUserIdAndIsShared_shared() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndIsShared(1L, true);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("6-2: 本人の非共有テンプレート取得")
    void testFindByUserIdAndIsShared_private() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndIsShared(1L, false);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("6-3: 該当なし")
    void testFindByUserIdAndIsShared_empty() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndIsShared(999L, true);
        assertThat(results).isEmpty();
    }


    // 7. findByCategoryIdAndIsShared
    @Test
    @DisplayName("7-1: 共有カテゴリ内テンプレート取得")
    void testFindByCategoryIdAndIsShared_normal() {
        List<MessageTemplateEntity> results = mapper.findByCategoryIdAndIsShared(1L, true);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("7-2: 該当なし")
    void testFindByCategoryIdAndIsShared_empty() {
        List<MessageTemplateEntity> results = mapper.findByCategoryIdAndIsShared(999L, true);
        assertThat(results).isEmpty();
    }


    // 8. findByUserIdAndCategoryIdAndIsShared
    @Test
    @DisplayName("8-1: 全条件一致で取得成功")
    void testFindByUserIdAndCategoryIdAndIsShared_normal() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndCategoryIdAndIsShared(1L, 1L, true);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("8-2: 該当なし")
    void testFindByUserIdAndCategoryIdAndIsShared_empty() {
        List<MessageTemplateEntity> results = mapper.findByUserIdAndCategoryIdAndIsShared(3L, 1L, false);
        assertThat(results).isEmpty();
    }


    // 9. existsByName
    @Test
    @DisplayName("9-1: タイトルが存在する場合 TRUE")
    void testExistsByName_true() {
        boolean exists = mapper.existsByName("test");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("9-2: タイトルが存在しない場合 FALSE")
    void testExistsByName_false() {
        boolean exists = mapper.existsByName("unknown");
        assertThat(exists).isFalse();
    }


    // 10. insert
    @Test
    @DisplayName("10-1: 新規作成成功")
    void testInsert_normal() {
        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setUserId(1L);
        template.setTitle("new_title");
        template.setContent("new_content");
        template.setCategoryId(1L);
        template.setShared(true);

        mapper.insert(template);

        assertThat(template.getId()).isNotNull();
    }


    // 11. update
    @Test
    @DisplayName("11-1: 編集成功")
    void testUpdate_normal() {
        MessageTemplateEntity template = mapper.findById(1L).get();
        template.setTitle("updated_title");
        template.setContent("updated_content");
        template.setCategoryId(2L);
        template.setShared(false);

        mapper.update(template);

        MessageTemplateEntity updated = mapper.findById(1L).get();
        assertThat(updated.getTitle()).isEqualTo("updated_title");
        assertThat(updated.getContent()).isEqualTo("updated_content");
        assertThat(updated.getCategoryId()).isEqualTo(2L);
        assertThat(updated.isShared()).isFalse();
    }

    @Test
    @DisplayName("11-2: 存在しない id の更新は件数 0")
    void testUpdate_notFound() {
        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(999L);
        template.setTitle("x");
        template.setContent("y");
        template.setCategoryId(1L);
        template.setShared(true);

        mapper.update(template);

        assertThat(mapper.findById(999L)).isEmpty();
    }


    // 12. deleteById
    @Test
    @DisplayName("12-1: 削除成功")
    void testDeleteById_normal() {
        mapper.deleteById(1L);
        assertThat(mapper.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("12-2: 存在しない id の削除は件数 0")
    void testDeleteById_notFound() {
        mapper.deleteById(999L);
        assertThat(mapper.findById(999L)).isEmpty();
    }
}
