package com.rc.readcompass.review.mapper;

import com.rc.readcompass.book.Book;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.ReviewCreateRequest;
import com.rc.readcompass.review.dto.ReviewDto;
import com.rc.readcompass.review.dto.ReviewLikeDto;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.entity.ReviewLike;
import com.rc.readcompass.review.entity.ReviewRanking;
import com.rc.readcompass.user.Entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // ReviewCreateRequest -> Review
    @Mapping(target = "book", source = "book")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "rating", source = "request.rating")
    @Mapping(target = "likeCnt", constant = "0")
    @Mapping(target = "commentCnt", constant = "0")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target="createdAt", ignore = true)
    @Mapping(target="updatedAt", ignore = true)
    @Mapping(target="id", ignore = true)
    Review toEntity(
            ReviewCreateRequest request,
            Book book,
            UserRole.User user
    );

    // Review -> ReviewDto
    @Mapping(target = "bookId", source = "review.book.id")
    @Mapping(target = "bookTitle", source = "review.book.title")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "userNickname", source = "review.user.nickname")
    @Mapping(target = "likeCount", source = "review.likeCnt")
    @Mapping(target = "commentCount", source = "review.commentCnt")
    ReviewDto toDto(
            Review review,
            String bookThumbnailUrl,
            boolean likedByMe
    );

    // ReviewLike -> ReviewLikeDto
    @Mapping(target = "reviewId", source = "reviewLike.review.id")
    @Mapping(target = "userId", source = "reviewLike.user.id")
    ReviewLikeDto toLikeDto(
            ReviewLike reviewLike,
            boolean liked
    );

    // ReviewLike가 없는 경우 새로 생성하여 사용
    default ReviewLikeDto toLikeDto(UUID reviewId, UUID userId, boolean liked){
        return new ReviewLikeDto(reviewId, userId, liked);
    }

    // ReviewRanking -> PopularReviewDto
    @Mapping(target = "id", source = "reviewRanking.id")
    @Mapping(target = "reviewId", source = "reviewRanking.review.id")
    @Mapping(target = "bookId", source = "reviewRanking.review.book.id")
    @Mapping(target = "bookTitle", source = "reviewRanking.review.book.title")
    @Mapping(target = "bookThumbnailUrl", source = "bookThumbnailUrl")
    @Mapping(target = "userId", source = "reviewRanking.review.user.id")
    @Mapping(target = "userNickname", source = "reviewRanking.review.user.nickname")
    @Mapping(target = "reviewContent", source = "reviewRanking.review.content")
    @Mapping(target = "reviewRating", source = "reviewRanking.review.rating")
    @Mapping(target = "period", source = "reviewRanking.period")
    @Mapping(target = "createdAt", source = "reviewRanking.calculatedAt")
    @Mapping(target = "rank", source = "reviewRanking.rankPosition")
    @Mapping(target = "score", source = "reviewRanking.score")
    @Mapping(target = "likeCount", source = "reviewRanking.review.likeCnt")
    @Mapping(target = "commentCount", source = "reviewRanking.review.commentCnt")
    PopularReviewDto toPopularDto(
            ReviewRanking reviewRanking,
            String bookThumbnailUrl
    );
}