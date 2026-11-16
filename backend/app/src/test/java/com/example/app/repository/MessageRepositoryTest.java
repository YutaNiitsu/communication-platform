// package com.example.app.Mapper;

// import com.example.app.domain.channel.ChannelEntity;
// import com.example.app.domain.channel.ChannelMapper;
// import com.example.app.domain.message.MessageEntity;
// import com.example.app.domain.message.MessageMapper;
// import com.example.app.domain.user.UserEntity;
// import com.example.app.domain.user.UserMapper;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// import java.time.LocalDateTime;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;

// @DataJpaTest
// class MessageMapperTest {

//     @Autowired
//     private MessageMapper messageMapper;

//     @Autowired
//     private ChannelMapper channelMapper;

//     @Autowired
//     private UserMapper userMapper;

//     private ChannelEntity createChannel(String name) {
//         ChannelEntity c = new ChannelEntity();
//         c.setName(name);
//         return channelMapper.save(c);
//     }

//     private UserEntity createUser(String username) {
//         UserEntity u = new UserEntity();
//         u.setUsername(username);
//         u.setPasswordHash("pass");
//         u.setRole("user");
//         return userMapper.save(u);
//     }

//     private MessageEntity createMessage(ChannelEntity c, UserEntity u, String content, boolean deleted, LocalDateTime createdAt) {
//         MessageEntity m = new MessageEntity();
//         m.setChannel(c);
//         m.setUser(u);
//         m.setContent(content);
//         m.setDeletedFlag(deleted);
//         m.setCreatedAt(createdAt);
//         return messageMapper.save(m);
//     }

//     // ---------------------------------------------------------
//     // findByChannelAndDeletedFlagFalse
//     // ---------------------------------------------------------

//     @Test
//     void testFindByChannelAndDeletedFlagFalse_normal() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "m1", false, LocalDateTime.now());
//         createMessage(c, u, "m2", false, LocalDateTime.now());
//         createMessage(c, u, "deleted", true, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.findByChannelAndDeletedFlagFalse(c);

//         assertThat(result).hasSize(2);
//     }

//     @Test
//     void testFindByChannelAndDeletedFlagFalse_zero() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "deleted", true, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.findByChannelAndDeletedFlagFalse(c);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByChannelAndDeletedFlagFalse_nullChannel() {
//         assertThat(messageMapper.findByChannelAndDeletedFlagFalse(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // findByUserAndDeletedFlagFalse
//     // ---------------------------------------------------------

//     @Test
//     void testFindByUserAndDeletedFlagFalse_normal() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("bob");

//         createMessage(c, u, "m1", false, LocalDateTime.now());
//         createMessage(c, u, "m2", false, LocalDateTime.now());
//         createMessage(c, u, "deleted", true, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.findByUserAndDeletedFlagFalse(u);

//         assertThat(result).hasSize(2);
//     }

//     @Test
//     void testFindByUserAndDeletedFlagFalse_zero() {
//         UserEntity u = createUser("bob");

//         List<MessageEntity> result = messageMapper.findByUserAndDeletedFlagFalse(u);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByUserAndDeletedFlagFalse_nullUser() {
//         assertThat(messageMapper.findByUserAndDeletedFlagFalse(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // findByChannel
//     // ---------------------------------------------------------

//     @Test
//     void testFindByChannel_normal() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "m1", false, LocalDateTime.now());
//         createMessage(c, u, "m2", false, LocalDateTime.now());
//         createMessage(c, u, "deleted", true, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.findByChannel(c);

//         assertThat(result).hasSize(3);
//     }

//     @Test
//     void testFindByChannel_zero() {
//         ChannelEntity c = createChannel("general");

//         List<MessageEntity> result = messageMapper.findByChannel(c);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByChannel_null() {
//         assertThat(messageMapper.findByChannel(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // findByUser
//     // ---------------------------------------------------------

//     @Test
//     void testFindByUser_normal() {
//         UserEntity u = createUser("alice");
//         ChannelEntity c = createChannel("general");

//         createMessage(c, u, "m1", false, LocalDateTime.now());
//         createMessage(c, u, "m2", false, LocalDateTime.now());
//         createMessage(c, u, "deleted", true, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.findByUser(u);

//         assertThat(result).hasSize(3);
//     }

//     @Test
//     void testFindByUser_zero() {
//         UserEntity u = createUser("alice");

//         List<MessageEntity> result = messageMapper.findByUser(u);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByUser_null() {
//         assertThat(messageMapper.findByUser(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // searchMessages
//     // ---------------------------------------------------------

//     @Test
//     void testSearchMessages_keyword() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "error occurred", false, LocalDateTime.now());
//         createMessage(c, u, "normal message", false, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 "error", null, null, null, null);

//         assertThat(result).hasSize(1);
//     }

//     @Test
//     void testSearchMessages_channelId() {
//         ChannelEntity c1 = createChannel("general");
//         ChannelEntity c2 = createChannel("random");
//         UserEntity u = createUser("alice");

//         createMessage(c1, u, "m1", false, LocalDateTime.now());
//         createMessage(c2, u, "m2", false, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, c1.getId(), null, null, null);

//         assertThat(result).hasSize(1);
//     }

//     @Test
//     void testSearchMessages_userId() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u1 = createUser("alice");
//         UserEntity u2 = createUser("bob");

//         createMessage(c, u1, "m1", false, LocalDateTime.now());
//         createMessage(c, u2, "m2", false, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, null, u1.getId(), null, null);

//         assertThat(result).hasSize(1);
//     }

//     @Test
//     void testSearchMessages_dateRange() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         LocalDateTime now = LocalDateTime.now();
//         LocalDateTime old = now.minusDays(1);

//         createMessage(c, u, "old", false, old);
//         createMessage(c, u, "new", false, now);

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, null, null,
//                 now.minusHours(1),
//                 now.plusHours(1)
//         );

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).getContent()).isEqualTo("new");
//     }

//     @Test
//     void testSearchMessages_allConditions() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         LocalDateTime now = LocalDateTime.now();

//         createMessage(c, u, "error occurred", false, now);

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 "error", c.getId(), u.getId(),
//                 now.minusHours(1), now.plusHours(1)
//         );

//         assertThat(result).hasSize(1);
//     }

//     @Test
//     void testSearchMessages_keywordNull() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "m1", false, LocalDateTime.now());
//         createMessage(c, u, "m2", false, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, null, null, null, null);

//         assertThat(result).hasSize(2);
//     }

//     @Test
//     void testSearchMessages_dateNull() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "m1", false, LocalDateTime.now());
//         createMessage(c, u, "m2", false, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, null, null, null, null);

//         assertThat(result).hasSize(2);
//     }

//     @Test
//     void testSearchMessages_noMatch() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         createMessage(c, u, "hello", false, LocalDateTime.now());

//         List<MessageEntity> result = messageMapper.searchMessages(
//                 "error", null, null, null, null);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testSearchMessages_channelIdNotExists() {
//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, 999L, null, null, null);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testSearchMessages_userIdNotExists() {
//         List<MessageEntity> result = messageMapper.searchMessages(
//                 null, null, 999L, null, null);

//         assertThat(result).isEmpty();
//     }
// }
