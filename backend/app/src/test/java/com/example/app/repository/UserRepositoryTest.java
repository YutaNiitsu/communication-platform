// package com.example.app.Mapper;

// import com.example.app.domain.user.UserEntity;
// import com.example.app.domain.user.UserMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;

// @DataJpaTest
// class UserMapperTest {

//     @Autowired
//     private UserMapper userMapper;

//     private UserEntity createUser(String username, String password) {
//         UserEntity u = new UserEntity();
//         u.setUsername(username);
//         u.setPasswordHash(password);
//         u.setRole("user");
//         return userMapper.save(u);
//     }

//     // ---------------------------------------------------------
//     // 1. ユーザー名検索（存在）
//     // ---------------------------------------------------------
//     @Test
//     void testFindByUsername_exists() {
//         createUser("alice", "pass");

//         Optional<UserEntity> result = userMapper.findByUsername("alice");

//         assertThat(result).isPresent();
//         assertThat(result.get().getUsername()).isEqualTo("alice");
//     }

//     // ---------------------------------------------------------
//     // 2. ユーザー名検索（不存在）
//     // ---------------------------------------------------------
//     @Test
//     void testFindByUsername_notExists() {
//         Optional<UserEntity> result = userMapper.findByUsername("unknown");

//         assertThat(result).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 3. username が空文字 → Optional.empty
//     // ---------------------------------------------------------
//     @Test
//     void testFindByUsername_emptyString() {
//         Optional<UserEntity> result = userMapper.findByUsername("");

//         assertThat(result).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 4. username が null → Optional.empty
//     // ---------------------------------------------------------
//     @Test
//     void testFindByUsername_null() {
//         assertThat(userMapper.findByUsername(null)).isEmpty();
//     }
// }
