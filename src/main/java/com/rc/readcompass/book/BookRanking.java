package com.rc.readcompass.book;

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
 * 도서 인기 랭킹 스냅샷.
 * 매일 배치로 계산되며, calculated_at 기준 최신 데이터를 조회에 사용한다.
 *
 * 점수 = (해당 기간의 리뷰수 × 0.4) + (해당 기간의 평점 평균 × 0.6)
 */
@Entity
@Table(
    name = "tb_book_rankings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tb_book_rankings_book_period_calculated",
            columnNames = {"book_id", "period_type", "calculated_at"}
        ),
        @UniqueConstraint(
            name = "uk_tb_book_rankings_period_rank_calculated",
            columnNames = {"period_type", "rank_position", "calculated_at"}
        )
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookRanking extends BaseEntity {

    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID bookId;

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
