package com.example.app.service;

import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.channel.ChannelMemberResponseDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentionService  {

    private final ChannelService channelService;
    private final UserMapper userMapper;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public List<UserResponseDTO> getCandidates(String query, Long channelId) {

        // --- channelId が不正 ---
        if (channelId != null && channelId <= 0) {
            appLog.warn("Invalid channelId");
            throw new IllegalArgumentException("Invalid channelId");
        }

        // --- チャンネル指定あり ---
        if (channelId != null) {

            // チャンネル存在チェック（channelService 内で NotFoundException を投げる）
            List<ChannelMemberResponseDTO> channelMembers;
            try {
                channelMembers = channelService.getMembers(channelId);
            } catch (NotFoundException e) {
                appLog.warn("Channel not found");
                throw new NotFoundException("Channel not found");
            }

            // 絞り込み
            List<ChannelMemberResponseDTO> filtered = channelMembers.stream()
                    .filter(u -> query == null || query.isBlank() || u.username().startsWith(query))
                    .toList();

            return filtered.stream()
                .map(u -> new UserResponseDTO(
                        u.userId(),
                        u.username(),
                        u.role(),
                        u.isActive()
                ))
                .toList();
        }

        // --- チャンネル指定なし（全ユーザー検索） ---
        List<UserEntity> users;

        if (query == null || query.isBlank()) {
            users = userMapper.findAll();
        } else {
            users = userMapper.findByUsernameStartingWith(query);
        }

        return users.stream()
            .map(u -> new UserResponseDTO(
                    u.getId(),
                    u.getUsername(),
                    u.getRole(),
                    u.isActive()
            ))
            .toList();
    }
}
