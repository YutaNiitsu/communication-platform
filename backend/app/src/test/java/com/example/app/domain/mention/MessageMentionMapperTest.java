package com.example.app.domain.mention;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/message_mentions.sql"})
class MessageMentionMapperTest {

    @Autowired
    private MessageMentionMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で 1件取得成功")
    void testFindById_normal() {
        MessageMentionEntity result = mapper.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getMessageId()).isEqualTo(1L);
        assertThat(result.getMentionedUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("1-2: createdAt が正しく取得される")
    void testFindById_createdAt() {
        MessageMentionEntity result = mapper.findById(1L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は null")
    void testFindById_notFound() {
        MessageMentionEntity result = mapper.findById(999L);
        assertThat(result).isNull();
    }


    // 2. findByMessageId
    @Test
    @DisplayName("2-1: メッセージ内のメンション一覧取得")
    void testFindByMessageId_normal() {
        List<MessageMentionEntity> results = mapper.findByMessageId(1L);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("mentionedUserId").containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("2-2: メンションが存在しない場合は空リスト")
    void testFindByMessageId_empty() {
        List<MessageMentionEntity> results = mapper.findByMessageId(999L);
        assertThat(results).isEmpty();
    }


    // 3. findByMentionedUserId
    @Test
    @DisplayName("3-1: ユーザーがメンションされた一覧取得")
    void testFindByMentionedUserId_normal() {
        List<MessageMentionEntity> results = mapper.findByMentionedUserId(1L);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("messageId").containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("3-2: メンションされていないユーザーは空リスト")
    void testFindByMentionedUserId_empty() {
        List<MessageMentionEntity> results = mapper.findByMentionedUserId(999L);
        assertThat(results).isEmpty();
    }


    // 4. findByMessageIdAndMentionedUserId
    @Test
    @DisplayName("4-1: メッセージ＋ユーザーで 1件取得成功")
    void testFindByMessageIdAndMentionedUserId_normal() {
        MessageMentionEntity result = mapper.findByMessageIdAndMentionedUserId(1L, 1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("4-2: 組み合わせが存在しない場合 null")
    void testFindByMessageIdAndMentionedUserId_notFound() {
        MessageMentionEntity result = mapper.findByMessageIdAndMentionedUserId(1L, 999L);
        assertThat(result).isNull();
    }


    // 5. insert
    @Test
    @DisplayName("5-1: メンション追加成功")
    void testInsert_normal() {
        MessageMentionEntity mention = new MessageMentionEntity();
        mention.setMessageId(1L);
        mention.setMentionedUserId(3L);

        mapper.insert(mention);

        assertThat(mention.getId()).isNotNull();

        MessageMentionEntity saved = mapper.findById(mention.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getMessageId()).isEqualTo(1L);
        assertThat(saved.getMentionedUserId()).isEqualTo(3L);
    }


    // 6. deleteByMessageIdAndMentionedUserId
    @Test
    @DisplayName("6-1: 特定メンション削除成功")
    void testDeleteByMessageIdAndMentionedUserId_normal() {
        mapper.deleteByMessageIdAndMentionedUserId(1L, 2L);
        MessageMentionEntity result = mapper.findByMessageIdAndMentionedUserId(1L, 2L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("6-2: 該当レコードなしでも例外なし")
    void testDeleteByMessageIdAndMentionedUserId_notFound() {
        mapper.deleteByMessageIdAndMentionedUserId(1L, 999L);
        assertThat(mapper.findByMessageIdAndMentionedUserId(1L, 999L)).isNull();
    }


    // 7. deleteByMessageId
    @Test
    @DisplayName("7-1: メッセージ内全削除成功")
    void testDeleteByMessageId_normal() {
        mapper.deleteByMessageId(1L);
        assertThat(mapper.findByMessageId(1L)).isEmpty();
    }

    @Test
    @DisplayName("7-2: 該当メッセージなしでも例外なし")
    void testDeleteByMessageId_notFound() {
        mapper.deleteByMessageId(999L);
        assertThat(mapper.findByMessageId(999L)).isEmpty();
    }


    // 8. deleteByMentionedUserId
    @Test
    @DisplayName("8-1: ユーザーのメンション履歴全削除")
    void testDeleteByMentionedUserId_normal() {
        mapper.deleteByMentionedUserId(1L);
        assertThat(mapper.findByMentionedUserId(1L)).isEmpty();
    }

    @Test
    @DisplayName("8-2: 該当ユーザーなしでも例外なし")
    void testDeleteByMentionedUserId_notFound() {
        mapper.deleteByMentionedUserId(999L);
        assertThat(mapper.findByMentionedUserId(999L)).isEmpty();
    }
}
