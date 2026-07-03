package com.rc.readcompass.review.controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.jwt.entity.CustomUserDetails;
import com.rc.readcompass.review.dto.*;
import com.rc.readcompass.review.service.ReviewLikeService;
import com.rc.readcompass.review.service.ReviewRankingService;
import com.rc.readcompass.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * 요청자 식별은 클라이언트가 보내는 헤더/바디가 아니라,
 * JWTFilter 가 토큰 검증 후 SecurityContext 에 넣어준 인증 정보(@AuthenticationPrincipal)를 사용한다.
 * (헤더/바디의 userId 는 위조 가능하므로 신뢰하면 안 됨)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewRankingService reviewRankingService;

    @GetMapping("/popular")
    public SliceCursorPageResponse<PopularReviewDto> getPopularReviews(
            @RequestParam(defaultValue = "DAILY") PeriodType period,
            @RequestParam(defaultValue = "ASC") Order direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant after,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return reviewRankingService.getPopularReviews(
                period,
                direction,
                cursor,
                after,
                limit
        );
    }

    // 리뷰 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return reviewService.createReview(request, userDetails.getUserId());
    }

    // 리뷰 좋아요 / 좋아요 취소
    @PostMapping("/{reviewId}/like")
    public ReviewLikeDto likeReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return reviewLikeService.toggleLike(reviewId, userDetails.getUserId());
    }

    // 리뷰 상세 정보 조회
    @GetMapping("/{reviewId}")
    public ReviewDto getReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return reviewService.getReview(reviewId, userDetails.getUserId());
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ReviewDto updateReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return reviewService.updateReview(reviewId, userDetails.getUserId(), request);
    }

    // 리뷰 논리 삭제
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getUserId());
    }

    // 리뷰 물리 삭제
    @DeleteMapping("/{reviewId}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void permanentDeleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.permanentDeleteReview(reviewId, userDetails.getUserId());
    }

    // 리뷰 목록 조회
    @GetMapping
    public SliceCursorPageResponse<ReviewDto> searchReviews(
            @RequestParam(required = false) UUID userId,   // 특정 작성자의 리뷰만 보기 위한 "검색 필터" (요청자 식별 아님)
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

        return reviewService.searchReviews(request);
    }
}
