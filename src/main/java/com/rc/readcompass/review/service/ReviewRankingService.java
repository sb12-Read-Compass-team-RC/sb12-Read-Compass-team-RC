package com.rc.readcompass.review.service;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.PopularReviewSearchRequest;
import com.rc.readcompass.review.repository.reviewranking.ReviewRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewRankingService {
    private final ReviewRankingRepository reviewRankingRepository;

    public SliceCursorPageResponse<PopularReviewDto> getPopularReviews(
            PeriodType period,
            Order direction,
            String cursor,
            Instant after,
            int limit
    ){
        PopularReviewSearchRequest request = new PopularReviewSearchRequest(
                period == null ? PeriodType.DAILY : period,
                direction == null ? Order.ASC : direction,
                cursor,
                after,
                limit <=0 ? 50 : limit
        );

        return reviewRankingRepository.searchLatestPopularReviews(request);
    }
}
