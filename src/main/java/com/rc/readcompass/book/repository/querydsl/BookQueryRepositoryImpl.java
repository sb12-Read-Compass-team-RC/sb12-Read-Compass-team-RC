package com.rc.readcompass.book.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.book.entity.Book;
import com.rc.readcompass.book.entity.BookCategory;
import com.rc.readcompass.book.entity.QBinaryContent;
import com.rc.readcompass.book.entity.QBook;
import com.rc.readcompass.book.dto.BookDto;
import com.rc.readcompass.book.dto.BookSearchRequest;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookQueryRepositoryImpl implements BookQueryRepository {

  private final JPAQueryFactory queryFactory;

  private static final QBook b = QBook.book;
  private static final QBinaryContent bc = QBinaryContent.binaryContent;

  @Override
  public SliceCursorPageResponse<BookDto> searchCursor(BookSearchRequest request) {
    BooleanBuilder where = new BooleanBuilder();

    where.and(b.deleted.isFalse());

    if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      String keyword = request.getKeyword().trim();

      BooleanBuilder keywordCondition = new BooleanBuilder();

      keywordCondition.or(b.title.containsIgnoreCase(keyword));
      keywordCondition.or(b.author.containsIgnoreCase(keyword));
      keywordCondition.or(b.isbn.containsIgnoreCase(keyword));
      keywordCondition.or(b.category.stringValue().containsIgnoreCase(keyword));

      BookCategory matchedCategory = BookCategory.fromKeyword(keyword);
      if (matchedCategory != null) {
        keywordCondition.or(b.category.eq(matchedCategory));
      }

      where.and(keywordCondition);
    }

    if (request.getCategory() != null) {
      where.and(b.category.eq(request.getCategory()));
    }

    Order order = getOrder(request);
    int size = getSize(request);
    String sort = getSort(request.getOrderBy());

    Book cursorBook = findCursorBook(request.getCursor());

    if (cursorBook != null) {
      addCursorCondition(where, sort, order, cursorBook);
    }

    OrderSpecifier<?>[] orderBy = getOrderBy(sort, order);

    List<Tuple> rowsPlusOne = queryFactory
        .select(
            b.id,
            b.title,
            b.author,
            b.description,
            b.publisher,
            b.publishedDate,
            b.isbn,
            b.category,
            bc.renamedFileUrl,
            b.reviewCnt,
            b.rating,
            b.createdAt,
            b.updatedAt
        )
        .from(b)
        .leftJoin(bc).on(bc.book.id.eq(b.id))
        .where(where)
        .orderBy(orderBy)
        .limit(size + 1L)
        .fetch();

    boolean hasNext = rowsPlusOne.size() > size;

    List<Tuple> contentRows;
    if (hasNext) {
      contentRows = rowsPlusOne.subList(0, size);
    } else {
      contentRows = rowsPlusOne;
    }

    List<BookDto> content = new ArrayList<>();

    for (Tuple row : contentRows) {
      content.add(new BookDto(
          row.get(b.id),
          row.get(b.title),
          row.get(b.author),
          row.get(b.description),
          row.get(b.publisher),
          row.get(b.publishedDate),
          row.get(b.isbn),
          row.get(b.category),
          row.get(b.category).getLabel(),
          row.get(bc.renamedFileUrl),
          row.get(b.reviewCnt),
          row.get(b.rating),
          row.get(b.createdAt),
          row.get(b.updatedAt)
      ));
    }

    String nextCursor = null;
    Instant nextAfter = null;

    if (!content.isEmpty()) {
      BookDto last = content.get(content.size() - 1);
      nextCursor = last.id().toString();
      nextAfter = last.createdAt();
    }

    return SliceCursorPageResponse.<BookDto>builder()
        .content(content)
        .hasNext(hasNext)
        .size(size)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .totalElements(-1)
        .build();
  }

  private Book findCursorBook(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    UUID cursorId;

    try {
      cursorId = UUID.fromString(cursor);
    } catch (IllegalArgumentException e) {
      return null;
    }

    return queryFactory
        .selectFrom(b)
        .where(
            b.id.eq(cursorId),
            b.deleted.isFalse()
        )
        .fetchOne();
  }

  private Order getOrder(BookSearchRequest request) {
    if (request.getDirection() == null) {
      return Order.DESC;
    }

    if (request.getDirection() == Order.ASC) {
      return Order.ASC;
    }

    return Order.DESC;
  }

  private int getSize(BookSearchRequest request) {
    if (request.getLimit() == null || request.getLimit() <= 0) {
      return 10;
    }

    return request.getLimit();
  }

  private String getSort(String sort) {
    if ("publishedDate".equals(sort)) {
      return "publishedDate";
    }

    if ("rating".equals(sort)) {
      return "rating";
    }

    if ("reviewCount".equals(sort)) {
      return "reviewCount";
    }

    return "title";
  }

  private OrderSpecifier<?>[] getOrderBy(
      String sort,
      Order order
  ) {
    if ("publishedDate".equals(sort)) {
      return new OrderSpecifier[]{
          new OrderSpecifier<>(order, b.publishedDate),
          new OrderSpecifier<>(order, b.createdAt)
      };
    }

    if ("rating".equals(sort)) {
      return new OrderSpecifier[]{
          new OrderSpecifier<>(order, b.rating),
          new OrderSpecifier<>(order, b.createdAt)
      };
    }

    if ("reviewCount".equals(sort)) {
      return new OrderSpecifier[]{
          new OrderSpecifier<>(order, b.reviewCnt),
          new OrderSpecifier<>(order, b.createdAt)
      };
    }

    return new OrderSpecifier[]{
        new OrderSpecifier<>(order, b.title),
        new OrderSpecifier<>(order, b.createdAt)
    };
  }

  private void addCursorCondition(
      BooleanBuilder where,
      String sort,
      Order order,
      Book cursorBook
  ) {
    if ("publishedDate".equals(sort)) {
      if (order == Order.DESC) {
        where.and(
            b.publishedDate.lt(cursorBook.getPublishedDate())
                .or(
                    b.publishedDate.eq(cursorBook.getPublishedDate())
                        .and(b.createdAt.lt(cursorBook.getCreatedAt()))
                )
        );
      } else {
        where.and(
            b.publishedDate.gt(cursorBook.getPublishedDate())
                .or(
                    b.publishedDate.eq(cursorBook.getPublishedDate())
                        .and(b.createdAt.gt(cursorBook.getCreatedAt()))
                )
        );
      }
      return;
    }

    if ("rating".equals(sort)) {
      double cursorRating = cursorBook.getRating();

      if (order == Order.DESC) {
        where.and(
            b.rating.lt(cursorRating)
                .or(
                    b.rating.eq(cursorRating)
                        .and(b.createdAt.lt(cursorBook.getCreatedAt()))
                )
        );
      } else {
        where.and(
            b.rating.gt(cursorRating)
                .or(
                    b.rating.eq(cursorRating)
                        .and(b.createdAt.gt(cursorBook.getCreatedAt()))
                )
        );
      }
      return;
    }

    if ("reviewCount".equals(sort)) {
      int cursorReviewCount = cursorBook.getReviewCnt();

      if (order == Order.DESC) {
        where.and(
            b.reviewCnt.lt(cursorReviewCount)
                .or(
                    b.reviewCnt.eq(cursorReviewCount)
                        .and(b.createdAt.lt(cursorBook.getCreatedAt()))
                )
        );
      } else {
        where.and(
            b.reviewCnt.gt(cursorReviewCount)
                .or(
                    b.reviewCnt.eq(cursorReviewCount)
                        .and(b.createdAt.gt(cursorBook.getCreatedAt()))
                )
        );
      }
      return;
    }

    if (order == Order.DESC) {
      where.and(
          b.title.lt(cursorBook.getTitle())
              .or(
                  b.title.eq(cursorBook.getTitle())
                      .and(b.createdAt.lt(cursorBook.getCreatedAt()))
              )
      );
    } else {
      where.and(
          b.title.gt(cursorBook.getTitle())
              .or(
                  b.title.eq(cursorBook.getTitle())
                      .and(b.createdAt.gt(cursorBook.getCreatedAt()))
              )
      );
    }
  }
}