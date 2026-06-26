package com.rc.readcompass.comments.dto;

import com.querydsl.core.types.Order;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentSearchRequest(
    UUID reviewId,
    Order direction,
    UUID cursor,
    Instant after,
    Integer limit
) {
  public CommentSearchRequest {
    if (direction == null) direction = Order.DESC;
    if (limit == null || limit <= 0) limit = 50;
  }

}
