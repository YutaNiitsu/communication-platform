package com.example.app.dto.sync;

import java.util.List;

public record ReactionSummaryDTO(
        String reactionType,
        int count,
        List<Long> users
) {}
