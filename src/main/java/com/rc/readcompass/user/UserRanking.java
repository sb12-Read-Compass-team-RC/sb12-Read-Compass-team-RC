package com.rc.readcompass.user;

import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 파워 유저 랭킹 스냅샷.
 * 매일 배치로 계산되며, calculated_at 기준 최신 데이터를 조회에 사용한다.
 *
 * 활동 점수 = (해당 기간의 작성한 리뷰의 인기 점수 × 0.5)
 *           + (참여한 좋아요 수 × 0.2)
 *           + (참여한 댓글 수 × 0.3)
 */
@Entity
@Table(
    name = "tb_user_rankings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tb_user_rankings_user_period_calculated",
            columnNames = {"user_id", "period_type", "calculated_at"}
        ),
        @UniqueConstraint(
            name = "uk_tb_user_rankings_period_rank_calculated",
            columnNames = {"period_type", "rank_position", "calculated_at"}
        )
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRanking extends BaseEntity {

    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(nullable = false)
    private int rankPosition;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal score;

    @Column(nullable = false)
    private Instant calculatedAt;
}
