package com.rc.readcompass.review.mapper;

import com.rc.readcompass.book.Book;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.ReviewCreateRequest;
import com.rc.readcompass.review.dto.ReviewDto;
import com.rc.readcompass.review.dto.ReviewLikeDto;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.entity.ReviewLike;
import com.rc.readcompass.review.entity.ReviewRanking;
import com.rc.readcompass.user.User;
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
            User user
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
}