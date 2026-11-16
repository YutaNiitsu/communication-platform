// package com.example.app.domain.channel;

// import com.example.app.domain.user.UserEntity;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;
// import java.util.List;

// public interface ChannelMemberRepository extends JpaRepository<ChannelMemberEntity, Long> {

//     Optional<ChannelMemberEntity> findByChannelAndUser(ChannelEntity channel, UserEntity user);

//     List<ChannelMemberEntity> findByChannel(ChannelEntity channel);

//     List<ChannelMemberEntity> findByUser(UserEntity user);
// }