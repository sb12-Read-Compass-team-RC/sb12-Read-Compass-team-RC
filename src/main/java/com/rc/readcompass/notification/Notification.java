package com.rc.readcompass.notification;

import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * 알림.
 * schema에 updated_at 없음 — BaseEntity 상속.
 * 확인 완료 후 1주일 경과 시 배치로 물리 삭제됨.
 */
@Entity
@Table(name = "tb_notifications")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID reviewId;

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

    public void confirm() {
        this.confirmed = true;
    }
}
