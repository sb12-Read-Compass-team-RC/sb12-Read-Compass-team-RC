package com.rc.readcompass.review.entity;

import com.rc.readcompass.book.entity.Book;
import com.rc.readcompass.common.domain.BaseUpdatableEntity;
import com.rc.readcompass.user.Entity.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "tb_reviews")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id",nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserRole.User user;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Min(0)
    @Builder.Default
    @Column(name = "like_cnt", nullable = false)
    private int likeCnt = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "comment_cnt", nullable = false)
    private int commentCnt = 0;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // =====================================================
    // 도메인 메서드
    // =====================================================

    public void updateReview(String content, int rating) {
        validateContent (content);
        validateRating(rating);

        this.content = content;
        this.rating = rating;
    }

    private void validateContent(String content){
        if(content == null || content.isBlank()){
            throw new IllegalArgumentException("리뷰 내용은 비어있을 수 없습니다.");
        }
    }

    private void validateRating(int rating){
        if(rating < 1 || rating > 5){
            throw new IllegalArgumentException("평점은 1점 이상 5점 이하만 가능합니다.");
        }
    }

    public void incrementLikeCount() {
        this.likeCnt++;
    }

    public void decrementLikeCount() {
        if (this.likeCnt > 0) this.likeCnt--;
    }

    public void incrementCommentCount() {
        this.commentCnt++;
    }

    public void decrementCommentCount() {
        if (this.commentCnt > 0) this.commentCnt--;
    }

    /**
     * 논리 삭제.
     * 삭제 후에도 인기 도서/파워 유저 점수 산출에 포함됨.
     */
    public void softDelete() {
        this.deleted = true;
    }
}
