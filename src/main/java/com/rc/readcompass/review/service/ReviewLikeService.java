package com.rc.readcompass.review.service;

import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.notification.service.NotificationService;
import com.rc.readcompass.review.dto.ReviewLikeDto;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.entity.ReviewLike;
import com.rc.readcompass.review.mapper.ReviewMapper;
import com.rc.readcompass.review.repository.review.ReviewLikeRepository;
import com.rc.readcompass.review.repository.review.ReviewRepository;
import com.rc.readcompass.user.Repository.UserRepository;
import com.rc.readcompass.user.entity.User;
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
    private final NotificationService notificationService;

    // 리뷰 좋아요 - 좋아요를 추가하거나 취소
    @Transactional
    public ReviewLikeDto toggleLike(UUID reviewId, UUID requestUserId){
        Review review = reviewRepository.findActiveByIdForUpdate(reviewId)
                .orElseThrow(()-> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        User user =  userRepository.findById(requestUserId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        return reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)
                .map(reviewLike -> unlike(review.getId(), reviewLike,requestUserId))
                .orElseGet(()-> like(review,user));
    }

    private ReviewLikeDto unlike(UUID reviewId, ReviewLike reviewLike, UUID requestUserId) {
        reviewLikeRepository.delete(reviewLike);
        reviewRepository.decrementLikeCount(reviewId);

        return reviewMapper.toLikeDto(
                reviewId,
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
        reviewRepository.incrementLikeCount(review.getId());

        notificationService.createLikeNotification(review, user);

        return reviewMapper.toLikeDto(
                reviewLike,
                true
        );
    }
}
