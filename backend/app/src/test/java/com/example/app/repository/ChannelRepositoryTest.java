// package com.example.app.Mapper;

// import com.example.app.domain.channel.ChannelEntity;
// import com.example.app.domain.channel.ChannelMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;

// @DataJpaTest
// class ChannelMapperTest {

//     @Autowired
//     private ChannelMapper channelMapper;

//     private ChannelEntity createChannel(String name) {
//         ChannelEntity c = new ChannelEntity();
//         c.setName(name);
//         return channelMapper.save(c);
//     }

//     // ---------------------------------------------------------
//     // 1. チャンネル名検索（存在）
//     // ---------------------------------------------------------
//     @Test
//     void testFindByName_exists() {
//         createChannel("general");

//         Optional<ChannelEntity> result = channelMapper.findByName("general");

//         assertThat(result).isPresent();
//         assertThat(result.get().getName()).isEqualTo("general");
//     }

//     // ---------------------------------------------------------
//     // 2. チャンネル名検索（不存在）
//     // ---------------------------------------------------------
//     @Test
//     void testFindByName_notExists() {
//         Optional<ChannelEntity> result = channelMapper.findByName("unknown");

//         assertThat(result).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 3. name が null → Optional.empty
//     // ---------------------------------------------------------
//     @Test
//     void testFindByName_null() {
//         assertThat(channelMapper.findByName(null)).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 4. name が空文字 → Optional.empty
//     // ---------------------------------------------------------
//     @Test
//     void testFindByName_emptyString() {
//         Optional<ChannelEntity> result = channelMapper.findByName("");

//         assertThat(result).isEmpty();
//     }
// }
