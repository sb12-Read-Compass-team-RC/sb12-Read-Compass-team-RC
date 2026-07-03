package com.rc.readcompass.review.controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.jwt.entity.CustomUserDetails;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.ReviewCreateRequest;
import com.rc.readcompass.review.dto.ReviewDto;
import com.rc.readcompass.review.dto.ReviewLikeDto;
import com.rc.readcompass.review.dto.ReviewSearchRequest;
import com.rc.readcompass.review.dto.ReviewUpdateRequest;
import com.rc.readcompass.review.service.ReviewLikeService;
import com.rc.readcompass.review.service.ReviewRankingService;
import com.rc.readcompass.review.service.ReviewService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 요청자 식별은 클라이언트가 보내는 헤더/바디가 아니라,
 * JWTFilter가 토큰 검증 후 SecurityContext에 넣어준 인증 정보(@AuthenticationPrincipal)를 사용한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

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
        return ResponseEntity.ok(
                reviewRankingService.getPopularReviews(
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
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(request, userDetails.getUserId()));
    }

    // 리뷰 좋아요 / 좋아요 취소
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> likeReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                reviewLikeService.toggleLike(reviewId, userDetails.getUserId())
        );
    }

    // 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                reviewService.getReview(reviewId, userDetails.getUserId())
        );
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(
                        reviewId,
                        userDetails.getUserId(),
                        request
                )
        );
    }

    // 리뷰 논리 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 리뷰 물리 삭제
    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> permanentDeleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.permanentDeleteReview(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<SliceCursorPageResponse<ReviewDto>> searchReviews(
            @RequestParam(required = false) UUID userId, // 검색 필터용
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "DESC") Order direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant after,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
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
                userDetails.getUserId()
        );

        return ResponseEntity.ok(reviewService.searchReviews(request));
    }
}