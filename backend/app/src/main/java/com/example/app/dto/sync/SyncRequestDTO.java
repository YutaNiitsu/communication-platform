package com.example.app.dto.sync;

import java.util.List;

public record SyncRequestDTO(
        List<Long> channelIds,
        Long lastMessageId
) {}
