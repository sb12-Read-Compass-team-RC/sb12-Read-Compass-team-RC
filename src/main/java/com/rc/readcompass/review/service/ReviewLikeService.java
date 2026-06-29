package com.rc.readcompass.review.service;

import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.review.dto.ReviewLikeDto;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.entity.ReviewLike;
import com.rc.readcompass.review.exception.ReviewException;
import com.rc.readcompass.review.mapper.ReviewMapper;
import com.rc.readcompass.review.repository.review.ReviewLikeRepository;
import com.rc.readcompass.review.repository.review.ReviewRepository;
import com.rc.readcompass.user.UserRepository;
import com.rc.readcompass.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    // 리뷰 좋아요 - 좋아요를 추가하거나 취소
    @Transactional
    public ReviewLikeDto toggleLike(UUID reviewId, UUID requestUserId){
        Review review = reviewRepository.findActiveByIdForUpdate(reviewId)
                .orElseThrow(()-> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        User user =  userRepository.findById(requestUserId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        return reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)
                .map(reviewLike -> unlike(review, reviewLike,requestUserId))
                .orElseGet(()-> like(review,user));
    }

    private ReviewLikeDto unlike(Review review, ReviewLike reviewLike, UUID requestUserId) {
        reviewLikeRepository.delete(reviewLike);
        review.decrementLikeCount();

        return reviewMapper.toLikeDto(
                review.getId(),
                requestUserId,
                false
        );
    }

    private ReviewLikeDto like(Review review, User user){
        ReviewLike reviewLike = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();

        reviewLikeRepository.save(reviewLike);
        review.incrementLikeCount();

        return reviewMapper.toLikeDto(
                reviewLike,
                true
        );
    }
}
