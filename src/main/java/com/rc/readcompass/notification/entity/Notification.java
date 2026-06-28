package com.rc.readcompass.notification.entity;

import com.rc.readcompass.common.domain.BaseEntity;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_notifications")
@Getter
@SuperBuilder @ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @ToString.Exclude
    private Review review;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "noti_type", nullable = false, length = 30)
    private NotificationType notiType;

    @Builder.Default
    @Column(nullable = false)
    private boolean confirmed = false;

    // =====================================================
    // 도메인 메서드
    // =====================================================

    /**
     * 알림 확인 처리
     */
    public void confirm() {
        if (!confirmed) {
            this.confirmed = true;
        }
    }
}