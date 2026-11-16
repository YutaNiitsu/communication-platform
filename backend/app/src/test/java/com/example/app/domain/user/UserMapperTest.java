package com.example.app.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql({"/testdata/schema.sql", "/testdata/users.sql"})
class UserMapperTest {

    @Autowired
    private UserMapper mapper;

    // 1. findAll
    @Test
    @DisplayName("1-1: 全件取得成功")
    void testFindAll_normal() {
        List<UserEntity> results = mapper.findAll();
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("1-2: createdAt が正しく取得される")
    void testFindAll_createdAt() {
        List<UserEntity> results = mapper.findAll();
        assertThat(results.get(0).getCreatedAt()).isNotNull();
    }


    // 2. findById
    @Test
    @DisplayName("2-1: ID で取得成功")
    void testFindById_normal() {
        Optional<UserEntity> result = mapper.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("2-2: 存在しない ID は empty")
    void testFindById_notFound() {
        Optional<UserEntity> result = mapper.findById(999L);
        assertThat(result).isEmpty();
    }


    // 3. findByUsername
    @Test
    @DisplayName("3-1: username で取得成功")
    void testFindByUsername_normal() {
        Optional<UserEntity> result = mapper.findByUsername("alice");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("3-2: 存在しない username は empty")
    void testFindByUsername_notFound() {
        Optional<UserEntity> result = mapper.findByUsername("unknown");
        assertThat(result).isEmpty();
    }


    // 4. existsByUsername
    @Test
    @DisplayName("4-1: username が存在する → TRUE")
    void testExistsByUsername_true() {
        boolean exists = mapper.existsByUsername("alice");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("4-2: username が存在しない → FALSE")
    void testExistsByUsername_false() {
        boolean exists = mapper.existsByUsername("unknown");
        assertThat(exists).isFalse();
    }


    // 5. findByUsernameStartingWith
    @Test
    @DisplayName("5-1: 前方一致検索成功")
    void testFindByUsernameStartingWith_normal() {
        List<UserEntity> results = mapper.findByUsernameStartingWith("a");
        assertThat(results).extracting("username")
                .containsExactly("adam", "alice"); // 昇順
    }

    @Test
    @DisplayName("5-2: 該当なしは空リスト")
    void testFindByUsernameStartingWith_empty() {
        List<UserEntity> results = mapper.findByUsernameStartingWith("zzz");
        assertThat(results).isEmpty();
    }


    // 6. insert
    @Test
    @DisplayName("6-1: 新規作成成功")
    void testInsert_normal() {
        UserEntity user = new UserEntity();
        user.setUsername("charlie");
        user.setPasswordHash("hashX");
        user.setRole(1);
        user.setActive(true);

        mapper.insert(user);

        assertThat(user.getId()).isNotNull();

        Optional<UserEntity> saved = mapper.findById(user.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getUsername()).isEqualTo("charlie");
    }


    // 7. update
    @Test
    @DisplayName("7-1: 編集成功")
    void testUpdate_normal() {
        UserEntity user = mapper.findById(1L).get();
        user.setUsername("alice_updated");
        user.setPasswordHash("hash_updated");
        user.setRole(1);
        user.setActive(false);

        mapper.update(user);

        UserEntity updated = mapper.findById(1L).get();
        assertThat(updated.getUsername()).isEqualTo("alice_updated");
        assertThat(updated.getPasswordHash()).isEqualTo("hash_updated");
        assertThat(updated.getRole()).isEqualTo(1);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @DisplayName("7-2: 存在しない id の更新は件数 0")
    void testUpdate_notFound() {
        UserEntity user = new UserEntity();
        user.setId(999L);
        user.setUsername("x");
        user.setPasswordHash("y");
        user.setRole(0);
        user.setActive(true);

        mapper.update(user);

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
