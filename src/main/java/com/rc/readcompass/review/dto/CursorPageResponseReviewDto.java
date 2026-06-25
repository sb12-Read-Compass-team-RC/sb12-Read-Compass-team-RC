package com.rc.readcompass.review.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseReviewDto(
        List<ReviewDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {

}
