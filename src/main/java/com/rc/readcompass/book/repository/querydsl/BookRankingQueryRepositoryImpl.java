package com.rc.readcompass.book.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.book.dto.PopularBookDto;
import com.rc.readcompass.book.entity.QBinaryContent;
import com.rc.readcompass.book.entity.QBook;
import com.rc.readcompass.book.entity.QBookRanking;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRankingQueryRepositoryImpl implements BookRankingQueryRepository {

  private final JPAQueryFactory queryFactory;

  private static final QBookRanking br = QBookRanking.bookRanking;
  private static final QBook b = QBook.book;
  private static final QBinaryContent bc = QBinaryContent.binaryContent;

  @Override
  public SliceCursorPageResponse<PopularBookDto> searchCursor(
      PeriodType periodType,
      Order direction,
      String cursor,
      Instant after,
      int limit
  ) {
    // ── 1. 최신 calculated_at 조회 ───────────────────────────────
    Instant latestCalculatedAt = queryFactory
        .select(br.calculatedAt.max())
        .from(br)
        .where(br.periodType.eq(periodType))
        .fetchOne();

    if (latestCalculatedAt == null) {
      return SliceCursorPageResponse.<PopularBookDto>builder()
          .content(List.of())
          .hasNext(false)
          .size(limit)
          .nextCursor(null)
          .nextAfter(null)
          .totalElements(0)
          .build();
    }

    // ── 2. WHERE 조건 ─────────────────────────────────────────────
    BooleanBuilder where = new BooleanBuilder();
    where.and(br.periodType.eq(periodType));
    where.and(br.calculatedAt.eq(latestCalculatedAt));
    where.and(b.deleted.isFalse());

    // ── 3. 커서 조건 ──────────────────────────────────────────────
    if (cursor != null && !cursor.isBlank()) {
      applyCursorCondition(where, cursor, after, direction);
    }

    // ── 4. 정렬 ───────────────────────────────────────────────────
    OrderSpecifier<?>[] orderBy = {
        new OrderSpecifier<>(direction, br.rankPosition),
        new OrderSpecifier<>(direction, br.createdAt)
    };

    // ── 5. 쿼리 실행 ──────────────────────────────────────────────
    List<Tuple> rowsPlusOne = queryFactory
        .select(
            br.id,
            br.bookId,
            b.title,
            b.author,
            bc.renamedFileUrl,
            br.periodType.stringValue(),
            br.rankPosition,    // int → Integer 박싱
            br.score,           // BigDecimal
            b.reviewCnt,        // int → Integer 박싱
            b.rating,           // double → Double 박싱
            br.createdAt
        )
        .from(br)
        .join(b).on(b.id.eq(br.bookId))
        .leftJoin(bc).on(bc.book.id.eq(b.id))
        .where(where)
        .orderBy(orderBy)
        .limit(limit + 1L)
        .fetch();

    boolean hasNext = rowsPlusOne.size() > limit;
    List<Tuple> contentRows = hasNext
        ? rowsPlusOne.subList(0, limit)
        : rowsPlusOne;

    // ── 6. Tuple → DTO 변환 ───────────────────────────────────────
    List<PopularBookDto> content = new ArrayList<>();
    for (Tuple row : contentRows) {
      PeriodType period = row.get(br.periodType);
      Integer rankRaw = row.get(br.rankPosition);
      Integer reviewCntRaw = row.get(b.reviewCnt);
      BigDecimal scoreRaw = row.get(br.score);
      Double ratingRaw = row.get(b.rating);

      content.add(new PopularBookDto(
          row.get(br.id),
          row.get(br.bookId),
          row.get(b.title),
          row.get(b.author),
          row.get(bc.renamedFileUrl),
          period,
          rankRaw != null ? rankRaw.longValue() : null,           // Long
          scoreRaw != null ? scoreRaw.doubleValue() : null,       // Double
          reviewCntRaw != null ? reviewCntRaw.longValue() : null, // Long
          ratingRaw,                                               // Double (이미 래퍼)
          row.get(br.createdAt)
      ));
    }

    // ── 7. 다음 커서 계산 ─────────────────────────────────────────
    String nextCursor = null;
    Instant nextAfter = null;
    if (!content.isEmpty()) {
      PopularBookDto last = content.get(content.size() - 1);
      nextCursor = last.rank() != null ? String.valueOf(last.rank()) : null;
      nextAfter = last.createdAt();
    }

    return SliceCursorPageResponse.<PopularBookDto>builder()
        .content(content)
        .hasNext(hasNext)
        .size(limit)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .totalElements(0)
        .build();
  }

  private void applyCursorCondition(BooleanBuilder where, String cursor,
      Instant after, Order direction) {
    int cursorRank;
    try {
      cursorRank = Integer.parseInt(cursor);
    } catch (NumberFormatException e) {
      return;
    }

    BooleanBuilder cond = new BooleanBuilder();
    if (direction == Order.ASC) {
      cond.or(br.rankPosition.gt(cursorRank));
      if (after != null) {
        cond.or(br.rankPosition.eq(cursorRank).and(br.createdAt.gt(after)));
      }
    } else {
      cond.or(br.rankPosition.lt(cursorRank));
      if (after != null) {
        cond.or(br.rankPosition.eq(cursorRank).and(br.createdAt.lt(after)));
      }
    }
    where.and(cond);
  }
}