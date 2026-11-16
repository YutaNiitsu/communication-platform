// package com.example.app.Mapper;

// import com.example.app.domain.channel.ChannelEntity;
// import com.example.app.domain.channel.ChannelMemberEntity;
// import com.example.app.domain.channel.ChannelMemberMapper;
// import com.example.app.domain.user.UserEntity;
// import com.example.app.domain.user.UserMapper;
// import com.example.app.domain.channel.ChannelMapper;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// import java.util.List;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;

// @DataJpaTest
// class ChannelMemberMapperTest {

//     @Autowired
//     private ChannelMemberMapper channelMemberMapper;

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

//     private ChannelMemberEntity createMember(ChannelEntity c, UserEntity u) {
//         ChannelMemberEntity m = new ChannelMemberEntity();
//         m.setChannel(c);
//         m.setUser(u);
//         return channelMemberMapper.save(m);
//     }

//     // ---------------------------------------------------------
//     // 1. findByChannelAndUser
//     // ---------------------------------------------------------

//     @Test
//     void testFindByChannelAndUser_exists() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");
//         createMember(c, u);

//         Optional<ChannelMemberEntity> result =
//                 channelMemberMapper.findByChannelAndUser(c, u);

//         assertThat(result).isPresent();
//     }

//     @Test
//     void testFindByChannelAndUser_notExists() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u = createUser("alice");

//         Optional<ChannelMemberEntity> result =
//                 channelMemberMapper.findByChannelAndUser(c, u);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByChannelAndUser_channelNull() {
//         UserEntity u = createUser("alice");

//         Optional<ChannelMemberEntity> result =
//             channelMemberMapper.findByChannelAndUser(null, u);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByChannelAndUser_userNull() {
//         ChannelEntity c = createChannel("general");

//         Optional<ChannelMemberEntity> result =
//                 channelMemberMapper.findByChannelAndUser(c, null);

//         assertThat(result).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 2. findByChannel
//     // ---------------------------------------------------------

//     @Test
//     void testFindByChannel_multiple() {
//         ChannelEntity c = createChannel("general");
//         UserEntity u1 = createUser("alice");
//         UserEntity u2 = createUser("bob");

//         createMember(c, u1);
//         createMember(c, u2);

//         List<ChannelMemberEntity> result = channelMemberMapper.findByChannel(c);

//         assertThat(result).hasSize(2);
//     }

//     @Test
//     void testFindByChannel_empty() {
//         ChannelEntity c = createChannel("general");

//         List<ChannelMemberEntity> result = channelMemberMapper.findByChannel(c);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByChannel_null() {
//                 assertThat(channelMemberMapper.findByChannel(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 3. findByUser
//     // ---------------------------------------------------------

//     @Test
//     void testFindByUser_multiple() {
//         UserEntity u = createUser("alice");
//         ChannelEntity c1 = createChannel("general");
//         ChannelEntity c2 = createChannel("random");

//         createMember(c1, u);
//         createMember(c2, u);

//         List<ChannelMemberEntity> result = channelMemberMapper.findByUser(u);

//         assertThat(result).hasSize(2);
//     }

//     @Test
//     void testFindByUser_empty() {
//         UserEntity u = createUser("alice");

//         List<ChannelMemberEntity> result = channelMemberMapper.findByUser(u);

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void testFindByUser_null() {
//         assertThat(channelMemberMapper.findByUser(null)).isEmpty();        
//     }
// }
