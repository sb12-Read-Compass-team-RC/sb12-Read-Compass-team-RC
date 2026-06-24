package com.rc.readcompass.review;

import com.rc.readcompass.common.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(
    name = "tb_reviews",
    // 도서당 사용자 1명의 리뷰 1개 제약
    uniqueConstraints = @UniqueConstraint(
        name = "uk_tb_reviews_book_user",
        columnNames = {"book_id", "user_id"}
    )
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseUpdatableEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Builder.Default
    @Column(name = "like_cnt", nullable = false)
    private int likeCnt = 0;

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
        this.content = content;
        this.rating = rating;
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
