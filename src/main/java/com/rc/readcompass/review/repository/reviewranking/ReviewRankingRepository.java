package com.rc.readcompass.review.repository.reviewranking;

import com.rc.readcompass.review.entity.ReviewRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRankingRepository extends JpaRepository<ReviewRanking, UUID>, ReviewRankingRepositoryCustom {
    void deleteAllByReviewId(UUID reviewId);
}
