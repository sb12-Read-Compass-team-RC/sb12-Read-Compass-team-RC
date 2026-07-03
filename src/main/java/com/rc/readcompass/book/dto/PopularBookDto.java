package com.rc.readcompass.book.dto;

import com.rc.readcompass.common.PeriodType;
import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    PeriodType period,
    Long rank,          // int64
    Double score,
    Long reviewCount,   // int64
    Double rating,
    Instant createdAt
) {}