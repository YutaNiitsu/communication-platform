package com.example.app.dto.channel;

public record ChannelEditRequestDTO(
    String name,
    int status // 0: アクティブ, 1: アーカイブ, 2: 削除
) {}
