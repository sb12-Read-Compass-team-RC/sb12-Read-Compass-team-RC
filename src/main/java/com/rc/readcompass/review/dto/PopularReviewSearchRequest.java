package com.rc.readcompass.review.dto;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;

import java.time.Instant;

public record PopularReviewSearchRequest(
        PeriodType period,
        Order direction,
        String cursor,
        Instant after,
        int limit
) {

}
