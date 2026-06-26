package com.rc.readcompass.user.dto;

import java.time.Instant;
import java.util.UUID;

public record PowerUserDto(
        UUID userId,
        String nickname,
        String period,
        Instant createdAt,
        Long rank,
        Double score,
        Double reviewScoreSum,
        Long likeCount,
        Long commentCount
) {}
