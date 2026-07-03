package com.rc.readcompass.notification.dto;

import com.querydsl.core.types.Order;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationSearchRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    UUID userId,
    Order direction,
    UUID cursor,
    Instant after,
    Integer limit
) {
  public NotificationSearchRequest {
    if (direction == null) direction = Order.DESC;
    if (limit == null || limit <= 0) limit = 20;
  }
}
