package com.rc.readcompass.comments.entity;

import com.rc.readcompass.common.domain.BaseUpdatableEntity;
import com.rc.readcompass.review.Review;
import com.rc.readcompass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_comments")
@Getter
@SuperBuilder @ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @ToString.Exclude
    private Review review;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // =====================================================
    // 도메인 메서드
    // =====================================================

    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 논리 삭제.
     * 삭제 후에도 인기 리뷰/파워 유저 점수 산출에 포함됨.
     */
    public void softDelete() {
        this.deleted = true;
    }
}
