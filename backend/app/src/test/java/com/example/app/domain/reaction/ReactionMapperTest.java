package com.example.app.domain.reaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/message_reactions.sql"})
class ReactionMapperTest {

    @Autowired
    private ReactionMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で取得成功")
    void testFindById_normal() {
        ReactionEntity result = mapper.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getReactionType()).isEqualTo("like");
    }

    @Test
    @DisplayName("1-2: createdAt が正しく取得される")
    void testFindById_createdAt() {
        ReactionEntity result = mapper.findById(1L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は null")
    void testFindById_notFound() {
        ReactionEntity result = mapper.findById(999L);
        assertThat(result).isNull();
    }


    // 2. findByMessageIdAndUserIdAndReactionType
    @Test
    @DisplayName("2-1: 組み合わせで取得成功")
    void testFindByMessageIdAndUserIdAndReactionType_normal() {
        Optional<ReactionEntity> result =
                mapper.findByMessageIdAndUserIdAndReactionType(1L, 1L, "like");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("2-2: 該当なしは empty")
    void testFindByMessageIdAndUserIdAndReactionType_empty() {
        Optional<ReactionEntity> result =
                mapper.findByMessageIdAndUserIdAndReactionType(1L, 1L, "love");
        assertThat(result).isEmpty();
    }


    // 3. findByMessageId
    @Test
    @DisplayName("3-1: メッセージのリアクション一覧取得")
    void testFindByMessageId_normal() {
        List<ReactionEntity> results = mapper.findByMessageId(1L);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("3-2: 該当なしは空リスト")
    void testFindByMessageId_empty() {
        List<ReactionEntity> results = mapper.findByMessageId(999L);
        assertThat(results).isEmpty();
    }


    // 4. insert
    @Test
    @DisplayName("4-1: リアクション追加成功")
    void testInsert_normal() {
        ReactionEntity reaction = new ReactionEntity();
        reaction.setMessageId(1L);
        reaction.setUserId(3L);
        reaction.setReactionType("like");

        mapper.insert(reaction);

        assertThat(reaction.getId()).isNotNull();

        ReactionEntity saved = mapper.findById(reaction.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getReactionType()).isEqualTo("like");
    }


    // 5. deleteByMessageIdAndUserIdAndReactionType
    @Test
    @DisplayName("5-1: 特定リアクション削除成功")
    void testDeleteByMessageIdAndUserIdAndReactionType_normal() {
        mapper.deleteByMessageIdAndUserIdAndReactionType(1L, 1L, "like");
        assertThat(mapper.findByMessageIdAndUserIdAndReactionType(1L, 1L, "like"))
                .isEmpty();
    }

    @Test
    @DisplayName("5-2: 該当なしは削除件数 0")
    void testDeleteByMessageIdAndUserIdAndReactionType_empty() {
        mapper.deleteByMessageIdAndUserIdAndReactionType(1L, 999L, "like");
        assertThat(mapper.findByMessageIdAndUserIdAndReactionType(1L, 999L, "like"))
                .isEmpty();
    }


    // 6. deleteByMessageId
    @Test
    @DisplayName("6-1: メッセージ内全削除成功")
    void testDeleteByMessageId_normal() {
        mapper.deleteByMessageId(1L);
        List<ReactionEntity> results = mapper.findByMessageId(1L);
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("6-2: 該当なしは削除件数 0")
    void testDeleteByMessageId_empty() {
        mapper.deleteByMessageId(999L);
        List<ReactionEntity> results = mapper.findByMessageId(999L);
        assertThat(results).isEmpty();
    }


    // 7. deleteByUserId
    @Test
    @DisplayName("7-1: ユーザーのリアクション履歴削除成功")
    void testDeleteByUserId_normal() {
        mapper.deleteByUserId(1L);
        List<ReactionEntity> results = mapper.findByMessageId(1L);
        assertThat(results).extracting("userId").doesNotContain(1L);
    }

    @Test
    @DisplayName("7-2: 該当なしは削除件数 0")
    void testDeleteByUserId_empty() {
        mapper.deleteByUserId(999L);
        List<ReactionEntity> results = mapper.findByMessageId(1L);
        assertThat(results).hasSize(2); // 元データのまま
    }
}
