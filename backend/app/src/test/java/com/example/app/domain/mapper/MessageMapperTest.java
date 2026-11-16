// package com.example.app.domain.mapper;

// import com.example.app.dto.message.MessageResponseDTO;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.test.context.jdbc.Sql;

// import java.time.LocalDateTime;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;

// @MybatisTest
// @Sql({"/testdata/schema.sql", "/testdata/messages.sql"}) // ★ テストデータ投入
// class MessageMapperTest {

//     @Autowired
//     private MessageMapper mapper;

//     // 1. キーワード検索
//     @Test
//     @DisplayName("1: キーワード検索で該当メッセージのみ返る")
//     void testKeywordSearch() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 "error", null, null, null, null
//         );
//         assertThat(results).hasSize(1);
//         assertThat(results.get(0).content()).contains("error");
//     }

//     // 2. チャンネル検索
//     @Test
//     @DisplayName("2: channelId=1 のメッセージのみ返る")
//     void testChannelSearch() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, 1L, null, null, null
//         );
//         assertThat(results).allMatch(m -> m.channelId() == 1L);
//     }

//     // 3. ユーザー検索
//     @Test
//     @DisplayName("3: userId=1 のメッセージのみ返る")
//     void testUserSearch() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, null, 1L, null, null
//         );
//         assertThat(results).allMatch(m -> m.userId() == 1L);
//     }

//     // 4. 日付範囲検索
//     @Test
//     @DisplayName("4: from～to の範囲内のみ返る")
//     void testDateRangeSearch() {
//         LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
//         LocalDateTime to   = LocalDateTime.of(2024, 12, 31, 23, 59);

//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, null, null, from, to
//         );

//         assertThat(results).allMatch(m ->
//                 !m.createdAt().isBefore(from) &&
//                 !m.createdAt().isAfter(to)
//         );
//     }

//     // 5. 全条件 AND 検索
//     @Test
//     @DisplayName("5: 全条件一致で 1 件返る")
//     void testAllConditions() {
//         LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
//         LocalDateTime to   = LocalDateTime.of(2024, 12, 31, 23, 59);

//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 "error", 1L, 1L, from, to
//         );

//         assertThat(results).hasSize(1);
//     }

//     // 6. keyword=null → 全件返る
//     @Test
//     @DisplayName("6: keyword=null で全件返る")
//     void testKeywordNull() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, null, null, null, null
//         );
//         assertThat(results).hasSize(3); // testdata の件数
//     }

//     // 7. 日付 null → 全件返る
//     @Test
//     @DisplayName("7: from=null, to=null で全件返る")
//     void testDateNull() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, null, null, null, null
//         );
//         assertThat(results).hasSize(3);
//     }

//     // 8. 全条件不一致 → 空リスト
//     @Test
//     @DisplayName("8: 全条件不一致で空リスト")
//     void testNoMatch() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 "no_match", 999L, 999L,
//                 LocalDateTime.of(2000,1,1,0,0),
//                 LocalDateTime.of(2000,1,2,0,0)
//         );
//         assertThat(results).isEmpty();
//     }

//     // 9. channelId=999 → 空リスト
//     @Test
//     @DisplayName("9: channelId=999 で空リスト")
//     void testChannelNotFound() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, 999L, null, null, null
//         );
//         assertThat(results).isEmpty();
//     }

//     // 10. userId=999 → 空リスト
//     @Test
//     @DisplayName("10: userId=999 で空リスト")
//     void testUserNotFound() {
//         List<MessageResponseDTO> results = mapper.searchMessages(
//                 null, null, 999L, null, null
//         );
//         assertThat(results).isEmpty();
//     }
// }
