package com.rc.readcompass.review.dto;

import com.querydsl.core.types.Order;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

public record ReviewSearchRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        UUID userId,

        @NotNull(message = "도서 ID는 필수입니다.")
        UUID bookId,

        String keyword,

        @Pattern(regexp="createdAt|rating", message="정렬 기준은 createdAd 또는 rating만 가능합니다.")
        String orderBy,

        Order direction,
        String cursor,
        Instant after,

        @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
        @Max(value = 100, message = "limit은 100 이하여야 합니다.")
        Integer limit,

        @NotNull(message="요청자 Id는 필수입니다.")
        UUID requestUserId
) {
}