package com.rc.readcompass.review.dto;

import com.querydsl.core.types.Order;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

public record ReviewSearchCondition(
        UUID userId,
        UUID bookId,
        String keyword,

        @Pattern(regexp = "createdAt|rating", message = "정렬 기준은 createdAt 또는 rating만 가능합니다.")
        String orderBy,

        Order direction,
        String cursor,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant after,

        @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
        @Max(value = 100, message = "limit은 100 이하여야 합니다.")
        Integer limit
) {
    public ReviewSearchRequest toRequest(UUID requestUserId) {
        return new ReviewSearchRequest(
                userId,
                bookId,
                keyword,
                orderBy != null ? orderBy : "createdAt",
                direction != null ? direction : Order.DESC,
                cursor,
                after,
                limit != null ? limit : 50,
                requestUserId
        );
    }
}