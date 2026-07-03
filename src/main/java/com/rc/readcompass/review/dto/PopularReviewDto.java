package com.rc.readcompass.review.dto;

import com.rc.readcompass.common.PeriodType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PopularReviewDto(
        UUID id,
        UUID reviewId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String reviewContent,
        Double reviewRating,
        PeriodType period,
        Instant createdAt,
        Long rank,
        BigDecimal score,
        Long likeCount,
        Long commentCount
) {
}
