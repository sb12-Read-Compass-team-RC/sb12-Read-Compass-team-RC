package com.rc.readcompass.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String content,
        int rating,
        Long likeCount,
        Long commentCount,
        boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
){
}
