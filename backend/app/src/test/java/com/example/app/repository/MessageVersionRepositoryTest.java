// package com.example.app.Mapper;

// import com.example.app.domain.channel.ChannelEntity;
// import com.example.app.domain.channel.ChannelMapper;
// import com.example.app.domain.message.MessageEntity;
// import com.example.app.domain.message.MessageMapper;
// import com.example.app.domain.message.MessageVersionEntity;
// import com.example.app.domain.message.MessageVersionMapper;
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
// class MessageVersionMapperTest {

//     @Autowired
//     private MessageVersionMapper messageVersionMapper;

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

//     private MessageEntity createMessage(ChannelEntity c, UserEntity u) {
//         MessageEntity m = new MessageEntity();
//         m.setChannel(c);
//         m.setUser(u);
//         m.setContent("original");
//         m.setDeletedFlag(false);
//         m.setCreatedAt(LocalDateTime.now());
//         return messageMapper.save(m);
//     }

//     private MessageVersionEntity createVersion(MessageEntity m, String content, LocalDateTime editedAt) {
//         MessageVersionEntity v = new MessageVersionEntity();
//         v.setMessage(m);
//         v.setBeforeContent(content);
//         v.setAfterContent(content);
//         v.setEditedAt(editedAt);
//         return messageVersionMapper.save(v);
//     }

//     // ---------------------------------------------------------
//     // findByMessageOrderByEditedAtAsc
//     // ---------------------------------------------------------

//     @Test
//     void testFindByMessageOrderByEditedAtAsc_normal() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");
//         MessageEntity m = createMessage(c, u);

//         createVersion(m, "v1", LocalDateTime.now().minusMinutes(10));
//         createVersion(m, "v2", LocalDateTime.now().minusMinutes(5));
//         createVersion(m, "v3", LocalDateTime.now());

//         List<MessageVersionEntity> result =
//                 messageVersionMapper.findByMessageOrderByEditedAtAsc(m);

//         assertThat(result).hasSize(3);
//         assertThat(result.get(0).getAfterContent()).isEqualTo("v1");
//         assertThat(result.get(1).getAfterContent()).isEqualTo("v2");
//         assertThat(result.get(2).getAfterContent()).isEqualTo("v3");
//     }

//     @Test
//     void testFindByMessageOrderByEditedAtAsc_one() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("bob");
//         MessageEntity m = createMessage(c, u);

//         createVersion(m, "v1", LocalDateTime.now());

//         List<MessageVersionEntity> result =
//                 messageVersionMapper.findByMessageOrderByEditedAtAsc(m);

//         assertThat(result).hasSize(1);
//     }

//     @Test
//     void testFindByMessageOrderByEditedAtAsc_zero() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("carol");
//         MessageEntity m = createMessage(c, u);

//         List<MessageVersionEntity> result =
//                 messageVersionMapper.findByMessageOrderByEditedAtAsc(m);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByMessageOrderByEditedAtAsc_null() {
//         assertThat(messageVersionMapper.findByMessageOrderByEditedAtAsc(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // findByMessageOrderByEditedAtDesc
//     // ---------------------------------------------------------

//     @Test
//     void testFindByMessageOrderByEditedAtDesc_normal() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("dave");
//         MessageEntity m = createMessage(c, u);

//         createVersion(m, "v1", LocalDateTime.now().minusMinutes(10));
//         createVersion(m, "v2", LocalDateTime.now().minusMinutes(5));
//         createVersion(m, "v3", LocalDateTime.now());

//         List<MessageVersionEntity> result =
//                 messageVersionMapper.findByMessageOrderByEditedAtDesc(m);

//         assertThat(result).hasSize(3);
//         assertThat(result.get(0).getAfterContent()).isEqualTo("v3");
//         assertThat(result.get(1).getAfterContent()).isEqualTo("v2");
//         assertThat(result.get(2).getAfterContent()).isEqualTo("v1");
//     }

//     @Test
//     void testFindByMessageOrderByEditedAtDesc_one() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("eve");
//         MessageEntity m = createMessage(c, u);

//         createVersion(m, "v1", LocalDateTime.now());

//         List<MessageVersionEntity> result =
//                 messageVersionMapper.findByMessageOrderByEditedAtDesc(m);

//         assertThat(result).hasSize(1);
//     }

//     @Test
//     void testFindByMessageOrderByEditedAtDesc_zero() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("frank");
//         MessageEntity m = createMessage(c, u);

//         List<MessageVersionEntity> result =
//                 messageVersionMapper.findByMessageOrderByEditedAtDesc(m);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByMessageOrderByEditedAtDesc_null() {
//         assertThat(messageVersionMapper.findByMessageOrderByEditedAtDesc(null)).isEmpty();
//     }
// }
