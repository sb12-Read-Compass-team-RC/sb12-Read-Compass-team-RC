package com.rc.readcompass.comments;

import com.rc.readcompass.common.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "tb_comments")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseUpdatableEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID reviewId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
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
