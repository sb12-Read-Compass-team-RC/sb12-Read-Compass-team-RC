package com.rc.readcompass.review.repository;

import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.CursorPageResponseReviewDto;
import com.rc.readcompass.review.dto.ReviewDto;
import com.rc.readcompass.review.dto.ReviewSearchRequest;

public interface ReviewRepositoryCustom {

    SliceCursorPageResponse<ReviewDto> searchCursorSortedFlat(ReviewSearchRequest request);
}
