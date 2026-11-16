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
@Sql({"/testdata/schema.sql", "/testdata/channels.sql"})
class ChannelMapperTest {

    @Autowired
    private ChannelMapper mapper;

    // 1. findById
    @Test
    @DisplayName("1-1: ID で 1件取得成功（status≠2）")
    void testFindById_normal() {
        Optional<ChannelEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("general");
    }

    @Test
    @DisplayName("1-2: 削除済みは取得されない（status=2）")
    void testFindById_deleted() {
        Optional<ChannelEntity> result = mapper.findById(2L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("1-3: 存在しない ID は empty")
    void testFindById_notFound() {
        Optional<ChannelEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 2. findByName
    @Test
    @DisplayName("2-1: name で 1件取得成功")
    void testFindByName_normal() {
        Optional<ChannelEntity> result = mapper.findByName("general");
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("2-2: 削除済みは取得されない")
    void testFindByName_deleted() {
        Optional<ChannelEntity> result = mapper.findByName("secret");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("2-3: 存在しない name は empty")
    void testFindByName_notFound() {
        Optional<ChannelEntity> result = mapper.findByName("unknown");
        assertThat(result).isEmpty();
    }


    // 3. findAll
    @Test
    @DisplayName("3-1: 全件取得成功（status≠2 のみ）")
    void testFindAll_normal() {
        List<ChannelEntity> results = mapper.findAll();
        assertThat(results).hasSize(2); // id=1,3 のみ
        assertThat(results).extracting("id").containsExactly(1L, 3L);
    }

    @Test
    @DisplayName("3-2: status≠2 が存在しない場合は空リスト")
    @Sql("/testdata/channels_only_deleted.sql")
    void testFindAll_empty() {
        List<ChannelEntity> results = mapper.findAll();
        assertThat(results).isEmpty();
    }


    // 4. insert
    @Test
    @DisplayName("4-1: 新規作成成功")
    void testInsert_normal() {
        ChannelEntity ch = new ChannelEntity();
        ch.setName("new_channel");
        ch.setStatus(0);

        mapper.insert(ch);

        assertThat(ch.getId()).isNotNull();

        Optional<ChannelEntity> saved = mapper.findById(ch.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("new_channel");
    }


    // 5. update
    @Test
    @DisplayName("5-1: name と status の更新成功")
    void testUpdate_normal() {
        ChannelEntity ch = mapper.findById(1L).get();
        ch.setName("updated");
        ch.setStatus(1);

        mapper.update(ch);

        ChannelEntity updated = mapper.findById(1L).get();
        assertThat(updated.getName()).isEqualTo("updated");
        assertThat(updated.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("5-2: 存在しない id の更新は件数 0")
    void testUpdate_notFound() {
        ChannelEntity ch = new ChannelEntity();
        ch.setId(999L);
        ch.setName("x");
        ch.setStatus(1);

        mapper.update(ch); // 例外は出ない

        Optional<ChannelEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 6. updateStatus
    @Test
    @DisplayName("6-1: status の更新成功")
    void testUpdateStatus_normal() {
        mapper.updateStatus(1L, 1);
        ChannelEntity updated = mapper.findById(1L).get();
        assertThat(updated.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("6-2: 存在しない id の更新は件数 0")
    void testUpdateStatus_notFound() {
        mapper.updateStatus(999L, 1);
        Optional<ChannelEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 7. deleteById
    @Test
    @DisplayName("7-1: 物理削除成功")
    void testDeleteById_normal() {
        mapper.deleteById(1L);
        Optional<ChannelEntity> result = mapper.findById(1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("7-2: 存在しない id の削除は件数 0")
    void testDeleteById_notFound() {
        mapper.deleteById(999L);
        Optional<ChannelEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 8. existsByName
    @Test
    @DisplayName("8-1: name が存在する場合 true")
    void testExistsByName_true() {
        boolean exists = mapper.existsByName("general");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("8-2: name が存在しない場合 false")
    void testExistsByName_false() {
        boolean exists = mapper.existsByName("unknown");
        assertThat(exists).isFalse();
    }
}
