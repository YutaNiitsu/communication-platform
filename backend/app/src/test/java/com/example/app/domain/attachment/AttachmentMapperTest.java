package com.example.app.domain.attachment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "spring.profiles.active=test",
    "logging.config=classpath:logback-test.xml"
})
@Sql({"/testdata/schema.sql", "/testdata/attachments.sql"})
class AttachmentMapperTest {

    @Autowired
    private AttachmentMapper mapper;

    // ---------------------------------------------------------
    // 1. insert
    // ---------------------------------------------------------
    @Test
    @DisplayName("1-1: 添付ファイル登録（insert）")
    void testInsert_normal() {
        AttachmentEntity a = new AttachmentEntity();
        a.setMessageId(1L);
        a.setOriginalFileName("new.pdf");
        a.setStoredFileName("new_stored.pdf");
        a.setFileUrl("/files/new_stored.pdf");
        a.setThumbnailUrl(null);
        a.setFileType("pdf");
        a.setFileSize(1234L);
        a.setCreatedAt(LocalDateTime.now());

        mapper.insert(a);

        List<AttachmentEntity> results = mapper.findOrderByCreatedAtAsc();
        assertThat(results).extracting("originalFileName").contains("new.pdf");
    }

    // ---------------------------------------------------------
    // 2. findById
    // ---------------------------------------------------------
    @Test
    @DisplayName("2-1: ID で取得（findById）")
    void testFindById_normal() {
        Optional<AttachmentEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getOriginalFileName()).isEqualTo("file1.pdf");
    }

    // ---------------------------------------------------------
    // 3. findOldest
    // ---------------------------------------------------------
    @Test
    @DisplayName("3-1: 古い順取得（findOldest）")
    void testFindOldest_normal() {
        List<AttachmentEntity> results = mapper.findOldest(1);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOriginalFileName()).isEqualTo("file1.pdf");
    }

    // ---------------------------------------------------------
    // 4. findLargest
    // ---------------------------------------------------------
    @Test
    @DisplayName("4-1: サイズ順取得（findLargest）")
    void testFindLargest_normal() {
        List<AttachmentEntity> results = mapper.findLargest(1);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFileSize()).isEqualTo(99999L);
    }

    // ---------------------------------------------------------
    // 5. findByMessageId
    // ---------------------------------------------------------
    @Test
    @DisplayName("5-1: messageId で取得（findByMessageId）")
    void testFindByMessageId_normal() {
        List<AttachmentEntity> results = mapper.findByMessageId(100L);
        assertThat(results).extracting("messageId").containsOnly(100L);
    }

    // ---------------------------------------------------------
    // 6. findOrderByCreatedAtAsc
    // ---------------------------------------------------------
    @Test
    @DisplayName("6-1: 全件取得（findOrderByCreatedAtAsc）")
    void testFindOrderByCreatedAtAsc_normal() {
        List<AttachmentEntity> results = mapper.findOrderByCreatedAtAsc();
        assertThat(results).isNotEmpty();
        assertThat(results).isSortedAccordingTo((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    // ---------------------------------------------------------
    // 7. delete
    // ---------------------------------------------------------
    @Test
    @DisplayName("7-1: 削除（delete）")
    void testDelete_normal() {
        mapper.delete(1L);
        Optional<AttachmentEntity> result = mapper.findById(1L);
        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------
    // 8. insert 異常（NOT NULL 制約違反）
    // ---------------------------------------------------------
    @Test
    @DisplayName("8-1: null 値で insert → DB 制約エラー")
    void testInsert_error_nullValue() {
        AttachmentEntity a = new AttachmentEntity();
        a.setMessageId(1L);
        a.setOriginalFileName(null); // NOT NULL

        assertThatThrownBy(() -> mapper.insert(a))
                .isInstanceOf(Exception.class);
    }

    // ---------------------------------------------------------
    // 9. findById 異常（empty）
    // ---------------------------------------------------------
    @Test
    @DisplayName("9-1: 存在しない ID → Optional.empty")
    void testFindById_notFound() {
        Optional<AttachmentEntity> result = mapper.findById(999999L);
        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------
    // 10. findOldest limit=0
    // ---------------------------------------------------------
    @Test
    @DisplayName("10-1: limit=0 → 空リスト")
    void testFindOldest_zeroLimit() {
        List<AttachmentEntity> results = mapper.findOldest(0);
        assertThat(results).isEmpty();
    }

    // ---------------------------------------------------------
    // 11. findByMessageId 異常（存在しない messageId）
    // ---------------------------------------------------------
    @Test
    @DisplayName("11-1: messageId 不存在 → 空リスト")
    void testFindByMessageId_notFound() {
        List<AttachmentEntity> results = mapper.findByMessageId(999999L);
        assertThat(results).isEmpty();
    }

    // ---------------------------------------------------------
    // 12. delete 異常（存在しない ID → 例外なし）
    // ---------------------------------------------------------
    @Test
    @DisplayName("12-1: 存在しない ID の delete → 例外なし")
    void testDelete_notFound_noError() {
        assertThatCode(() -> mapper.delete(999999L))
                .doesNotThrowAnyException();
    }

    // ---------------------------------------------------------
    // 13. DB 障害（疑似再現）
    // ---------------------------------------------------------
    @Test
    @DisplayName("13-1: DB 障害時 → 例外発生")
    @Sql(statements = "DROP TABLE message_attachments;")
    void testDbError() {
        assertThatThrownBy(() -> mapper.findOrderByCreatedAtAsc())
                .isInstanceOf(Exception.class);
    }
}
