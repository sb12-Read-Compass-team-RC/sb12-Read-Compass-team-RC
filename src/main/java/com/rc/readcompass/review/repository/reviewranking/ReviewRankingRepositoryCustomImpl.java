package com.rc.readcompass.review.repository.reviewranking;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.book.entity.QBinaryContent;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.PopularReviewDto;
import com.rc.readcompass.review.dto.PopularReviewSearchRequest;
import com.rc.readcompass.review.entity.QReviewRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRankingRepositoryCustomImpl implements ReviewRankingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QReviewRanking rr = QReviewRanking.reviewRanking;
    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;

    @Override
    public SliceCursorPageResponse<PopularReviewDto> searchLatestPopularReviews(
            PopularReviewSearchRequest request
    ) {
        PeriodType period = request.period() == null
                ? PeriodType.DAILY
                : request.period();

        Order direction = request.direction() == null
                ? Order.ASC
                : request.direction();

        int size = request.limit() > 0
                ? request.limit()
                : 50;

        /*
         * 핵심:
         * 조회할 때마다 해당 period의 최신 calculatedAt을 DB에서 읽는다.
         * 그래서 배치가 tb_review_rankings에 새 스냅샷을 insert하면
         * 다음 조회부터 자동으로 최신 랭킹을 바라본다.
         */
        Instant latestCalculatedAt = queryFactory
                .select(rr.calculatedAt.max())
                .from(rr)
                .where(rr.period.eq(period))
                .fetchOne();

        if (latestCalculatedAt == null) {
            return new SliceCursorPageResponse<PopularReviewDto>(
                    List.of(),
                    null,
                    null,
                    size,
                    0L,
                    false
            );
        }

        BooleanBuilder where = new BooleanBuilder();
        where.and(rr.period.eq(period));
        where.and(rr.calculatedAt.eq(latestCalculatedAt));
        where.and(rr.review.deleted.isFalse());
        where.and(rr.review.book.deleted.isFalse());

        if (request.cursor() != null && !request.cursor().isBlank()) {
            int cursorRank = parseCursorRank(request.cursor());

            if (direction == Order.ASC) {
                where.and(rr.rankPosition.gt(cursorRank));
            } else {
                where.and(rr.rankPosition.lt(cursorRank));
            }
        }

        List<PopularReviewDto> fetched = queryFactory
                .select(Projections.constructor(
                        PopularReviewDto.class,
                        rr.id,
                        rr.review.id,
                        rr.review.book.id,
                        rr.review.book.title,
                        binaryContent.renamedFileUrl,
                        rr.review.user.id,
                        rr.review.user.nickname,
                        rr.review.content,
                        rr.review.rating.doubleValue(),
                        rr.period,
                        rr.calculatedAt,
                        rr.rankPosition.longValue(),
                        rr.score,
                        rr.review.likeCnt.longValue(),
                        rr.review.commentCnt.longValue()
                ))
                .from(rr)
                .join(rr.review)
                .join(rr.review.book)
                .join(rr.review.user)
                .leftJoin(binaryContent)
                .on(binaryContent.book.id.eq(rr.review.book.id))
                .where(where)
                .orderBy(direction == Order.ASC
                        ? rr.rankPosition.asc()
                        : rr.rankPosition.desc())
                .limit(size + 1L)
                .fetch();

        boolean hasNext = fetched.size() > size;

        List<PopularReviewDto> content = hasNext
                ? fetched.subList(0, size)
                : fetched;

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !content.isEmpty()) {
            PopularReviewDto last = content.get(content.size() - 1);
            nextCursor = String.valueOf(last.rank());
            nextAfter = last.createdAt();
        }

        Long totalElements = queryFactory
                .select(rr.count())
                .from(rr)
                .where(
                        rr.period.eq(period),
                        rr.calculatedAt.eq(latestCalculatedAt),
                        rr.review.deleted.isFalse(),
                        rr.review.book.deleted.isFalse()
                )
                .fetchOne();

        return new SliceCursorPageResponse<PopularReviewDto>(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                totalElements == null ? 0L : totalElements,
                hasNext
        );
    }

    private int parseCursorRank(String cursor) {
        try {
            return Integer.parseInt(cursor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("인기 리뷰 커서는 rank 숫자여야 합니다.");
        }
    }
}