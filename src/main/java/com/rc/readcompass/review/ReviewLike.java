package com.rc.readcompass.review;

import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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

    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID reviewId;

    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID userId;
}
