package com.rc.readcompass.review.entity;

import com.rc.readcompass.common.PeriodType;import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 인기 리뷰 랭킹 스냅샷.
 * 매일 배치로 계산되며, calculated_at 기준 최신 데이터를 조회에 사용한다.
 * 점수 = (해당 기간의 좋아요 수 × 0.3) + (해당 기간의 댓글 수 × 0.7)
 * 인기 순위 TOP 10 진입 시 리뷰 작성자에게 알림 발송.
 */
@Entity
@Table(
    name = "tb_review_rankings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tb_review_rankings_review_period_calculated",
            columnNames = {"review_id", "period_type", "calculated_at"}
        ),
        @UniqueConstraint(
            name = "uk_tb_review_rankings_period_rank_calculated",
            columnNames = {"period_type", "rank_position", "calculated_at"}
        )
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRanking extends BaseEntity {

    @Column(name = "review_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID reviewId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20,updatable = false)
    private PeriodType period;

    @Min(1)
    @Column(name = "rank_position", nullable = false,updatable = false)
    private int rankPosition;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2,updatable = false)
    private BigDecimal score;

    @NotNull
    @Column(name = "calculated_at", nullable = false, updatable = false)
    private Instant calculatedAt;
}
