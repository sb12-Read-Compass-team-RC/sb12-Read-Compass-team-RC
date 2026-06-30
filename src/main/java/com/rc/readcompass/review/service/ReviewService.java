package com.rc.readcompass.review.service;

import com.querydsl.core.types.Order;
import com.rc.readcompass.book.entity.BinaryContent;
import com.rc.readcompass.book.entity.Book;
import com.rc.readcompass.book.repository.BinaryContentRepository;
import com.rc.readcompass.book.repository.BookRepository;
import com.rc.readcompass.comments.entity.Comment;
import com.rc.readcompass.comments.repository.CommentRepository;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.review.dto.*;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.entity.ReviewLike;
import com.rc.readcompass.review.entity.ReviewRanking;
import com.rc.readcompass.review.exception.ReviewException;
import com.rc.readcompass.review.mapper.ReviewMapper;
import com.rc.readcompass.review.repository.review.ReviewLikeRepository;
import com.rc.readcompass.review.repository.review.ReviewRepository;
import com.rc.readcompass.review.repository.reviewranking.ReviewRankingRepository;
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final CommentRepository commentRepository;
    private final ReviewRankingRepository reviewRankingRepository;
//    private final NotificationRepository notificationRepository;


    private final ReviewMapper reviewMapper;

    private static final int DEFAULT_LIMIT = 50;

    // 리뷰 등록
    @Transactional
    public ReviewDto createReview(ReviewCreateRequest request) {

        Book book = getBook(request.bookId());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 논리 삭제된 리뷰를 제외하고 중복 검사
        if (reviewRepository.existsByBookIdAndUserIdAndDeletedFalse(request.bookId(), request.userId())) {
            throw new ReviewException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = reviewMapper.toEntity(request, book, user);

        try {
            Review savedReview = reviewRepository.save(review);
            // 도서에 리뷰 갯수 및 평점 계산 요청
            book.addReview(savedReview.getRating());
            return toDto(savedReview, request.userId());

        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 unique 제약에 걸리는 경우 대비
            throw new ReviewException(ErrorCode.REVIEW_ALREADY_EXISTS, e);
        }
    }

    // 리뷰 상세 정보 조회
    public ReviewDto getReview(UUID reviewId, UUID requestUserId) {
        Review review = getActiveReview(reviewId);
        return toDto(review, requestUserId);
    }

    // 리뷰 목록 조회
    public SliceCursorPageResponse<ReviewDto> searchReviews(ReviewSearchRequest request) {
        ReviewSearchRequest normalizedRequest = normalize(request);

        validateCursor(
                normalizedRequest.orderBy(),
                normalizedRequest.cursor(),
                normalizedRequest.after()
        );

        return reviewRepository.searchCursorSortedFlat(normalizedRequest);
    }

    // 리뷰 수정
    @Transactional
    public ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest request) {
        Review review = getActiveReview(reviewId);
        Book book = getBook(review.getBook().getId());
        int oldRating = review.getRating();
        // review 소유자와 요청자가 같은지 확인
        validateOwner(review, requestUserId);

        review.updateReview(request.content(), request.rating());

        int newRating = review.getRating();
        // BookEntity 평점 수정
        book.updateRating(oldRating,newRating);

        return toDto(review, requestUserId);
    }

    // 리뷰 논리 삭제
    @Transactional
    public void deleteReview(UUID reviewId, UUID requestUserId) {
        Review review = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(()-> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
        validateOwner(review, requestUserId);

        review.softDelete();
    }

    // 리뷰 물리 삭제
    @Transactional
    public void permanentDeleteReview(UUID reviewId, UUID requestUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        validateOwner(review, requestUserId);

        if(!review.isDeleted()){
            throw new ReviewException(ErrorCode.REVIEW_NOT_DELETED);
        }

        Book book = getBook(review.getBook().getId());
        int rating = review.getRating();

        // 같이 사라져야 할 것들 좋아요, 댓글, 알림, 랭킹 스냅샷
        reviewLikeRepository.deleteAllByReviewId(reviewId);
        commentRepository.deleteAllByReviewId(reviewId);
//        notificationRepository.deleteAllByReviewId(reviewId);
        reviewRankingRepository.deleteAllByReviewId(reviewId);

        reviewRepository .delete(review);

        book.removeReview(rating);
    }

    private Review getActiveReview(UUID reviewId) {
        return reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private Book getBook(UUID bookId) {
        return bookRepository.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    }

    private void validateOwner(Review review, UUID requestUserId) {
        if (!review.getUser().getId().equals(requestUserId)) {
            throw new ReviewException(ErrorCode.REVIEW_FORBIDDEN);
        }
    }

    private ReviewDto toDto(Review review, UUID requestUserId) {
        boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(
                review.getId(),
                requestUserId
        );

        String bookThumbnailUrl = binaryContentRepository.findByBookId(review.getBook().getId())
                .map(BinaryContent::getRenamedFileUrl)
                .orElse(null);

        return reviewMapper.toDto(review, bookThumbnailUrl, likedByMe);
    }

    private ReviewSearchRequest normalize(ReviewSearchRequest request) {
        String orderBy = request.orderBy() == null || request.orderBy().isBlank()
                ? "createdAt"
                : request.orderBy();

        Order direction = request.direction() == null
                ? Order.DESC
                : request.direction();

        Integer limit = request.limit() == null
                ? DEFAULT_LIMIT
                : request.limit();

        String keyword = request.keyword() == null || request.keyword().isBlank()
                ? null
                : request.keyword().trim();

        return new ReviewSearchRequest(
                request.userId(),
                request.bookId(),
                keyword,
                orderBy,
                direction,
                request.cursor(),
                request.after(),
                limit,
                request.requestUserId()
        );
    }

    private void validateCursor(String orderBy, String cursor, Instant after) {
        if (cursor == null || cursor.isBlank()) {
            return;
        }

        if ("rating".equals(orderBy)) {
            try {
                int rating = Integer.parseInt(cursor);

                if (rating < 1 || rating > 5) {
                    throw new IllegalArgumentException("rating 커서는 1 이상 5 이하이어야 합니다." );
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("rating 커서 형식이 올바르지 않습니다." );
            }

            if (after == null) {
                throw new IllegalArgumentException("rating 정렬에서는 after 값이 필요합니다." );
            }
        }

        if ("createdAt".equals(orderBy)) {
            try {
                Instant.parse(cursor);
            } catch (Exception e) {
                throw new IllegalArgumentException("createdAt 커서 형식이 올바르지 않습니다." );
            }
        }
    }
}