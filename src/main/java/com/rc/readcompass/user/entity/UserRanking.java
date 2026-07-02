package com.rc.readcompass.user.entity;

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
 * 파워 유저 랭킹 스냅샷 (tb_user_rankings).
 * 매일 배치(별도 배치 프로젝트)로 계산되며, calculated_at 기준 최신 데이터를 조회에 사용한다.
 *
 * 활동 점수 = (해당 기간의 작성한 리뷰의 인기 점수 × 0.5)
 *           + (참여한 좋아요 수 × 0.2)
 *           + (참여한 댓글 수 × 0.3)
 *
 * 주의: 스키마(tb_user_rankings)에는 rank_position 과 최종 score 만 저장된다.
 *       배치는 리뷰 점수 합 / 좋아요 수 / 댓글 수로 score 를 산출하지만 세부값 컬럼은 두지 않으므로
 *       엔티티도 스키마와 동일하게 유지한다. (ddl-auto=validate 이므로 스키마와 반드시 일치해야 함)
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

    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20, updatable = false)
    private PeriodType periodType;

    @Column(name = "rank_position", nullable = false, updatable = false)
    private int rankPosition;

    /** 최종 활동 점수 */
    @Column(nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal score;

    @Column(name = "calculated_at", nullable = false, updatable = false)
    private Instant calculatedAt;
}
