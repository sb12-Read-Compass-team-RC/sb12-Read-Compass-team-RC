package com.rc.readcompass.book.dto;

import com.querydsl.core.types.Order;
import com.rc.readcompass.book.entity.BookCategory;
import lombok.Builder;

import java.time.Instant;

@Builder
public record BookSearchRequest(
    String keyword,
    BookCategory category,

    String sort,       // title / publishedDate / rating / reviewCount
    Order direction,   // ASC / DESC
    String cursor,     // 이전 응답의 nextCursor(UUID 문자열), 첫 페이지는 null
    Instant after,     // 이전 응답의 nextAfter(createdAt)
    Integer limit      // 페이지 사이즈
) {
}
