package com.rc.readcompass.review.repository.reviewranking;

import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.PopularReviewSearchRequest;

public interface ReviewRankingRepositoryCustom {

    SliceCursorPageResponse<PopularReviewDto> searchLatestPopularReviews(
            PopularReviewSearchRequest request
    );
}
