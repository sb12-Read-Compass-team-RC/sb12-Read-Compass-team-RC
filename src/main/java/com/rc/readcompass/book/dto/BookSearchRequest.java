package com.rc.readcompass.book.dto;

import com.querydsl.core.types.Order;
import com.rc.readcompass.book.entity.BookCategory;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class BookSearchRequest {

  private String keyword;
  private BookCategory category;

  private String orderBy = "title";    // title / publishedDate / rating / reviewCount
  private Order direction = Order.DESC;   // ASC / DESC
  private String cursor;     // 이전 응답의 nextCursor(UUID 문자열), 첫 페이지는 null

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant after;     // 이전 응답의 nextAfter(createdAt)

  private Integer limit = 50;      // 페이지 사이즈
}