package com.example.app.domain.channel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/channel_members.sql"})
class ChannelMemberMapperTest {

    @Autowired
    private ChannelMemberMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で 1件取得成功")
    void testFindById_normal() {
        Optional<ChannelMemberEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getChannelId()).isEqualTo(1L);
        assertThat(result.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("1-2: joinedAt が正しく取得される")
    void testFindById_joinedAt() {
        Optional<ChannelMemberEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は empty")
    void testFindById_notFound() {
        Optional<ChannelMemberEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 2. findByChannelAndUser
    @Test
    @DisplayName("2-1: チャンネル＋ユーザーで取得成功")
    void testFindByChannelAndUser_normal() {
        Optional<ChannelMemberEntity> result = mapper.findByChannelAndUser(1L, 1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("2-2: 組み合わせが存在しない場合 empty")
    void testFindByChannelAndUser_notFound() {
        Optional<ChannelMemberEntity> result = mapper.findByChannelAndUser(1L, 999L);
        assertThat(result).isEmpty();
    }


    // 3. findByChannel
    @Test
    @DisplayName("3-1: チャンネル所属ユーザー一覧取得")
    void testFindByChannel_normal() {
        List<ChannelMemberEntity> results = mapper.findByChannel(1L);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("userId").containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("3-2: 該当チャンネルに誰もいない場合は空リスト")
    void testFindByChannel_empty() {
        List<ChannelMemberEntity> results = mapper.findByChannel(999L);
        assertThat(results).isEmpty();
    }


    // 4. findByUser
    @Test
    @DisplayName("4-1: ユーザー所属チャンネル一覧取得")
    void testFindByUser_normal() {
        List<ChannelMemberEntity> results = mapper.findByUser(1L);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("channelId").containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("4-2: 該当ユーザーがどのチャンネルにも所属していない場合は空リスト")
    void testFindByUser_empty() {
        List<ChannelMemberEntity> results = mapper.findByUser(999L);
        assertThat(results).isEmpty();
    }


    // 5. insert
    @Test
    @DisplayName("5-1: チャンネル参加成功")
    void testInsert_normal() {
        ChannelMemberEntity member = new ChannelMemberEntity();
        member.setChannelId(1L);
        member.setUserId(3L);

        mapper.insert(member);

        assertThat(member.getId()).isNotNull();

        Optional<ChannelMemberEntity> saved = mapper.findById(member.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getChannelId()).isEqualTo(1L);
        assertThat(saved.get().getUserId()).isEqualTo(3L);
    }


    // 6. deleteByChannelAndUser
    @Test
    @DisplayName("6-1: チャンネル退出成功")
    void testDeleteByChannelAndUser_normal() {
        mapper.deleteByChannelAndUser(1L, 1L);
        Optional<ChannelMemberEntity> result = mapper.findByChannelAndUser(1L, 1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("6-2: 該当レコードなしでも例外なし")
    void testDeleteByChannelAndUser_notFound() {
        mapper.deleteByChannelAndUser(1L, 999L);
        // 例外が出ないことを確認
        assertThat(mapper.findByChannelAndUser(1L, 999L)).isEmpty();
    }


    // 7. deleteByChannel
    @Test
    @DisplayName("7-1: チャンネル内全メンバー削除")
    void testDeleteByChannel_normal() {
        mapper.deleteByChannel(1L);
        assertThat(mapper.findByChannel(1L)).isEmpty();
    }

    @Test
    @DisplayName("7-2: 該当チャンネルなしでも例外なし")
    void testDeleteByChannel_notFound() {
        mapper.deleteByChannel(999L);
        assertThat(mapper.findByChannel(999L)).isEmpty();
    }


    // 8. deleteByUser
    @Test
    @DisplayName("8-1: ユーザー所属チャンネル全削除")
    void testDeleteByUser_normal() {
        mapper.deleteByUser(1L);
        assertThat(mapper.findByUser(1L)).isEmpty();
    }

    @Test
    @DisplayName("8-2: 該当ユーザーなしでも例外なし")
    void testDeleteByUser_notFound() {
        mapper.deleteByUser(999L);
        assertThat(mapper.findByUser(999L)).isEmpty();
    }
}
