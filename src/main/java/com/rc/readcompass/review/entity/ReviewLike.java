package com.rc.readcompass.review.entity;

import com.rc.readcompass.common.domain.BaseEntity;
import com.rc.readcompass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 리뷰 좋아요.
 * 레코드 존재 여부로 좋아요 상태를 판단한다 (insert/delete 방식).
 * updatedAt 없음 — BaseEntity 상속.
 */
@Entity
@Table(
    name = "tb_review_likes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_tb_review_likes_review_user",
        columnNames = {"review_id", "user_id"}
    )
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false, updatable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;
}
