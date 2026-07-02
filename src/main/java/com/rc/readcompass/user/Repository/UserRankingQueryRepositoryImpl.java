package com.rc.readcompass.user.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.user.dto.PowerUserDto;
import com.rc.readcompass.user.dto.PowerUserSearchRequest;
import com.rc.readcompass.user.entity.QUser;
import com.rc.readcompass.user.entity.QUserRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 파워 유저 랭킹 조회.
 * BookRankingQueryRepositoryImpl 과 동일한 구조:
 *   1) 해당 기간 최신 calculated_at 조회
 *   2) 최신 스냅샷 + 커서 조건으로 rank 순 조회
 *   3) rank(보조: createdAt) 기준 커서 페이지네이션
 *
 * 참고: tb_user_rankings 스키마에는 rank_position 과 score(최종 활동 점수)만 저장된다.
 *       배치는 리뷰 점수 합 / 좋아요 수 / 댓글 수로 최종 score 를 계산하지만 그 세부값은 저장하지 않으므로,
 *       PowerUserDto 도 rank 와 score 만 노출한다.
 */
@Repository
@RequiredArgsConstructor
public class UserRankingQueryRepositoryImpl implements UserRankingQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QUserRanking ur = QUserRanking.userRanking;
    private static final QUser u = QUser.user;

    @Override
    public SliceCursorPageResponse<PowerUserDto> searchCursor(PowerUserSearchRequest request) {
        PeriodType period = request.period() == null ? PeriodType.DAILY : request.period();
        Order direction = request.direction() == null ? Order.ASC : request.direction();
        int limit = request.limit() > 0 ? request.limit() : 50;

        // 1. 해당 기간 최신 스냅샷 시각
        Instant latestCalculatedAt = queryFactory
                .select(ur.calculatedAt.max())
                .from(ur)
                .where(ur.periodType.eq(period))
                .fetchOne();

        if (latestCalculatedAt == null) {
            return SliceCursorPageResponse.<PowerUserDto>builder()
                    .content(List.of())
                    .nextCursor(null)
                    .nextAfter(null)
                    .size(limit)
                    .totalElements(0L)
                    .hasNext(false)
                    .build();
        }

        // 2. WHERE
        BooleanBuilder where = new BooleanBuilder();
        where.and(ur.periodType.eq(period));
        where.and(ur.calculatedAt.eq(latestCalculatedAt));
        where.and(u.deleted.isFalse());

        // 3. 커서 조건 (rank + 보조 커서 createdAt)
        if (request.cursor() != null && !request.cursor().isBlank()) {
            applyCursorCondition(where, request.cursor(), request.after(), direction);
        }

        // 4. 정렬 (rank, createdAt)
        OrderSpecifier<?>[] orderBy = {
                new OrderSpecifier<>(direction, ur.rankPosition),
                new OrderSpecifier<>(direction, ur.createdAt)
        };

        // 5. 조회 (limit + 1 로 다음 페이지 존재 여부 판단)
        List<Tuple> rows = queryFactory
                .select(
                        ur.userId,
                        u.nickname,
                        ur.periodType,
                        ur.createdAt,
                        ur.rankPosition,
                        ur.score
                )
                .from(ur)
                .join(u).on(u.id.eq(ur.userId))
                .where(where)
                .orderBy(orderBy)
                .limit(limit + 1L)
                .fetch();

        boolean hasNext = rows.size() > limit;
        List<Tuple> contentRows = hasNext ? rows.subList(0, limit) : rows;

        // 6. Tuple -> DTO
        List<PowerUserDto> content = new ArrayList<>();
        for (Tuple row : contentRows) {
            Integer rankRaw = row.get(ur.rankPosition);
            BigDecimal scoreRaw = row.get(ur.score);

            content.add(new PowerUserDto(
                    row.get(ur.userId),
                    row.get(u.nickname),
                    row.get(ur.periodType),
                    row.get(ur.createdAt),
                    rankRaw != null ? rankRaw.longValue() : null,
                    scoreRaw != null ? scoreRaw.doubleValue() : null
            ));
        }

        // 7. 다음 커서
        String nextCursor = null;
        Instant nextAfter = null;
        if (hasNext && !content.isEmpty()) {
            PowerUserDto last = content.get(content.size() - 1);
            nextCursor = last.rank() != null ? String.valueOf(last.rank()) : null;
            nextAfter = last.createdAt();
        }

        // 8. 전체 건수 (최신 스냅샷 기준)
        Long totalElements = queryFactory
                .select(ur.count())
                .from(ur)
                .join(u).on(u.id.eq(ur.userId))
                .where(
                        ur.periodType.eq(period),
                        ur.calculatedAt.eq(latestCalculatedAt),
                        u.deleted.isFalse()
                )
                .fetchOne();

        return SliceCursorPageResponse.<PowerUserDto>builder()
                .content(content)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(content.size())
                .totalElements(totalElements == null ? 0L : totalElements)
                .hasNext(hasNext)
                .build();
    }

    private void applyCursorCondition(BooleanBuilder where, String cursor,
                                      Instant after, Order direction) {
        int cursorRank;
        try {
            cursorRank = Integer.parseInt(cursor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("파워 유저 커서는 rank 숫자여야 합니다.");
        }

        BooleanBuilder cond = new BooleanBuilder();
        if (direction == Order.ASC) {
            cond.or(ur.rankPosition.gt(cursorRank));
            if (after != null) {
                cond.or(ur.rankPosition.eq(cursorRank).and(ur.createdAt.gt(after)));
            }
        } else {
            cond.or(ur.rankPosition.lt(cursorRank));
            if (after != null) {
                cond.or(ur.rankPosition.eq(cursorRank).and(ur.createdAt.lt(after)));
            }
        }
        where.and(cond);
    }
}
