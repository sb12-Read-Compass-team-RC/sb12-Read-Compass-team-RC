package com.rc.readcompass.review.controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.*;
import com.rc.readcompass.review.service.ReviewLikeService;
import com.rc.readcompass.review.service.ReviewRankingService;
import com.rc.readcompass.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final String REQUEST_USER_ID_HEADER = "Deokhugam-Request-User-ID";

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewRankingService reviewRankingService;

    @GetMapping("/popular")
    public ResponseEntity<SliceCursorPageResponse<PopularReviewDto>> getPopularReviews(
            @RequestParam(defaultValue = "DAILY") PeriodType period,
            @RequestParam(defaultValue = "ASC") Order direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant after,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(reviewRankingService.getPopularReviews(
                period,
                direction,
                cursor,
                after,
                limit
            )
        );
    }

    // 리뷰 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(request)
                );
    }

    // 리뷰 좋아요 / 좋아요 취소
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> likeReview(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewLikeService.toggleLike(reviewId, requestUserId)
                );
    }

    // 리뷰 상세 정보 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        return ResponseEntity.ok(reviewService.getReview(reviewId, requestUserId));
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.updateReview(reviewId, requestUserId, request));
    }

    // 리뷰 논리 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        reviewService.deleteReview(reviewId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    // 리뷰 물리 삭제
    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> permanentDeleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        reviewService.permanentDeleteReview(reviewId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    // 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<SliceCursorPageResponse<ReviewDto>> searchReviews(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "DESC") Order direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant after,
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        ReviewSearchRequest request = new ReviewSearchRequest(
                userId,
                bookId,
                keyword,
                orderBy,
                direction,
                cursor,
                after,
                limit,
                requestUserId
        );

        return ResponseEntity.ok(reviewService.searchReviews(request));
    }
}