package com.rc.readcompass.review.repository.reviewranking;

import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.PopularReviewSearchRequest;
import com.rc.readcompass.review.entity.ReviewRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRankingRepositoryCustom {

    SliceCursorPageResponse<PopularReviewDto> searchLatestPopularReviews(
            PopularReviewSearchRequest request
    );
}
