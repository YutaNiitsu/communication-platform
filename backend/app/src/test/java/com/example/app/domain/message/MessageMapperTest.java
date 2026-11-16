package com.example.app.domain.message;

import com.example.app.dto.message.MessageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/messages.sql"})
class MessageMapperTest {

    @Autowired
    private MessageMapper mapper;

    // 1. findAll
    @Test
    @DisplayName("1-1: 全件取得（status≠2）")
    void testFindAll_normal() {
        List<MessageEntity> results = mapper.findAll();
        assertThat(results).extracting("status").doesNotContain(2);
    }

    @Test
    @DisplayName("1-2: status≠2 が存在しない場合は空リスト")
    @Sql({"/testdata/schema.sql", "/testdata/messages_only_deleted.sql"})
    void testFindAll_empty() {
        List<MessageEntity> results = mapper.findAll();
        assertThat(results).isEmpty();
    }


    // 2. findById
    @Test
    @DisplayName("2-1: ID で取得成功")
    void testFindById_normal() {
        Optional<MessageEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("2-2: 存在しない ID は empty")
    void testFindById_notFound() {
        Optional<MessageEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 3. findByChannelAndStatus
    @Test
    @DisplayName("3-1: チャンネル＋ステータスで取得成功")
    void testFindByChannelAndStatus_normal() {
        List<MessageEntity> results = mapper.findByChannelAndStatus(1L, 0);
        assertThat(results).hasSize(10);
    }

    @Test
    @DisplayName("3-2: 該当なし")
    void testFindByChannelAndStatus_empty() {
        List<MessageEntity> results = mapper.findByChannelAndStatus(1L, 9);
        assertThat(results).isEmpty();
    }


    // 4. findByUserAndStatus
    @Test
    @DisplayName("4-1: ユーザー＋ステータスで取得成功")
    void testFindByUserAndStatus_normal() {
        List<MessageEntity> results = mapper.findByUserAndStatus(1L, 0);
        assertThat(results).hasSize(9);
    }

    @Test
    @DisplayName("4-2: 該当なし")
    void testFindByUserAndStatus_empty() {
        List<MessageEntity> results = mapper.findByUserAndStatus(999L, 0);
        assertThat(results).isEmpty();
    }


    // 5. findByChannel
    @Test
    @DisplayName("5-1: チャンネル内全メッセージ取得（status≠2）")
    void testFindByChannel_normal() {
        List<MessageEntity> results = mapper.findByChannel(1L);
        assertThat(results).extracting("status").doesNotContain(2);
    }

    @Test
    @DisplayName("5-2: 該当なし")
    void testFindByChannel_empty() {
        List<MessageEntity> results = mapper.findByChannel(999L);
        assertThat(results).isEmpty();
    }


    // 6. findByUser
    @Test
    @DisplayName("6-1: ユーザーの全メッセージ取得（status≠2）")
    void testFindByUser_normal() {
        List<MessageEntity> results = mapper.findByUser(1L);
        assertThat(results).extracting("status").doesNotContain(2);
    }

    @Test
    @DisplayName("6-2: 該当なし")
    void testFindByUser_empty() {
        List<MessageEntity> results = mapper.findByUser(999L);
        assertThat(results).isEmpty();
    }


    // 7. findByChannelIdAndIdGreaterThan
    @Test
    @DisplayName("7-1: 新着メッセージ取得成功")
    void testFindByChannelIdAndIdGreaterThan_normal() {
        List<MessageEntity> results = mapper.findByChannelIdAndIdGreaterThan(1L, 10L);
        assertThat(results).extracting("id").containsExactly(11L, 12L);
    }

    @Test
    @DisplayName("7-2: 新着なし")
    void testFindByChannelIdAndIdGreaterThan_empty() {
        List<MessageEntity> results = mapper.findByChannelIdAndIdGreaterThan(1L, 12L);
        assertThat(results).isEmpty();
    }


    // 8. findByReplyToMessageId
    @Test
    @DisplayName("8-1: 返信メッセージ取得成功")
    void testFindByReplyToMessageId_normal() {
        List<MessageEntity> results = mapper.findByReplyToMessageId(5L);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("8-2: 該当なし")
    void testFindByReplyToMessageId_empty() {
        List<MessageEntity> results = mapper.findByReplyToMessageId(999L);
        assertThat(results).isEmpty();
    }


    // 9. findPreviousMessages
    @Test
    @DisplayName("9-1: 過去メッセージ取得成功")
    void testFindPreviousMessages_normal() {
        List<MessageEntity> results = mapper.findPreviousMessages(1L, 50L, 10);
        assertThat(results).hasSize(10);
    }

    @Test
    @DisplayName("9-2: 該当なし")
    @Sql({"/testdata/schema.sql", "/testdata/messages_previous_empty.sql"})
    void testFindPreviousMessages_empty() {
        List<MessageEntity> results = mapper.findPreviousMessages(1L, 1L, 10);
        assertThat(results).isEmpty();
    }


    // 10. findLatestMessages
    @Test
    @DisplayName("10-1: 最新メッセージ取得成功")
    void testFindLatestMessages_normal() {
        List<MessageEntity> results = mapper.findLatestMessages(1L, 2);
        assertThat(results).extracting("id").containsExactly(12L, 11L);
    }


    // 11. findAllPreviousMessages
    @Test
    @DisplayName("11-1: 全件取得成功")
    void testFindAllPreviousMessages_normal() {
        List<MessageEntity> results = mapper.findAllPreviousMessages(1L, 12L);
        assertThat(results).extracting("id").contains(12L, 11L, 10L, 3L, 2L, 1L);
    }


    // 12. insert
    @Test
    @DisplayName("12-1: 新規作成成功")
    void testInsert_normal() {
        MessageEntity msg = new MessageEntity();
        msg.setChannelId(1L);
        msg.setUserId(1L);
        msg.setReplyToMessageId(null);
        msg.setContent("insert test");
        msg.setStatus(0);

        mapper.insert(msg);

        assertThat(msg.getId()).isNotNull();
    }


    // 13. update
    @Test
    @DisplayName("13-1: メッセージ更新成功")
    void testUpdate_normal() {
        MessageEntity msg = mapper.findById(1L).get();
        msg.setContent("updated");
        msg.setStatus(1);

        mapper.update(msg);

        MessageEntity updated = mapper.findById(1L).get();
        assertThat(updated.getContent()).isEqualTo("updated");
        assertThat(updated.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("13-2: 存在しない id の更新は件数 0")
    void testUpdate_notFound() {
        MessageEntity msg = new MessageEntity();
        msg.setId(999L);
        msg.setContent("x");
        msg.setStatus(1);

        mapper.update(msg);

        assertThat(mapper.findById(999L)).isEmpty();
    }


    // 14. logicalDelete
    @Test
    @DisplayName("14-1: 論理削除成功")
    void testLogicalDelete_normal() {
        mapper.logicalDelete(1L);
        MessageEntity msg = mapper.findById(1L).get();
        assertThat(msg.getStatus()).isEqualTo(2);
    }


    // 15. searchMessages
    @Test
    @DisplayName("15-1: keyword 検索")
    void testSearchMessages_keyword() {
        List<MessageDTO> results = mapper.searchMessages("test", null, null, null, null);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("15-2: channelId 条件")
    void testSearchMessages_channelId() {
        List<MessageDTO> results = mapper.searchMessages(null, 1L, null, null, null);
        assertThat(results).extracting("channelId").containsOnly(1L);
    }

    @Test
    @DisplayName("15-3: userId 条件")
    void testSearchMessages_userId() {
        List<MessageDTO> results = mapper.searchMessages(null, null, 1L, null, null);
        assertThat(results).extracting("userId").containsOnly(1L);
    }

    @Test
    @DisplayName("15-4: 日付範囲")
    void testSearchMessages_dateRange() {
        LocalDateTime from = LocalDateTime.parse("2024-01-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-01-02T00:00:00");

        List<MessageDTO> results = mapper.searchMessages(null, null, null, from, to);
        assertThat(results).allMatch(m -> !m.createdAt().isAfter(to));
    }

    @Test
    @DisplayName("15-5: 複合条件")
    void testSearchMessages_multi() {
        List<MessageDTO> results = mapper.searchMessages("test", 1L, 2L, null, null);
        assertThat(results).hasSize(1);
    }


    // 16. existsPreviousMessages
    @Test
    @DisplayName("16-1: 過去メッセージあり")
    void testExistsPreviousMessages_true() {
        Boolean exists = mapper.existsPreviousMessages(1L, 12L);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("16-2: 過去なし")
    void testExistsPreviousMessages_false() {
        Boolean exists = mapper.existsPreviousMessages(1L, 1L);
        assertThat(exists).isFalse();
    }
}
