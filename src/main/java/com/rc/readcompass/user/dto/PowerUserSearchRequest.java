package com.rc.readcompass.user.dto;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;

import java.time.Instant;

/**
 * 파워 유저 목록 조회 요청 파라미터.
 * 인기 리뷰(PopularReviewSearchRequest)와 동일한 커서 페이지네이션 규약을 따른다.
 */
public record PowerUserSearchRequest(
        PeriodType period,
        Order direction,
        String cursor,
        Instant after,
        int limit
) {}
