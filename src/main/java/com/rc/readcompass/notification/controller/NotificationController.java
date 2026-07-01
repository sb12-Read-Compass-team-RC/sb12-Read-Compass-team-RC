package com.rc.readcompass.notification.controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.notification.dto.NotificationDto;
import com.rc.readcompass.notification.dto.NotificationSearchRequest;
import com.rc.readcompass.notification.service.NotificationService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  // 알림 읽음 상태 업데이트
  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> confirmNotification(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
  ){
    NotificationDto response = notificationService.confirmNotification(notificationId, requestUserId);
    return ResponseEntity.ok(response);
  }

  // 모든 알림 읽음 처리
  @PatchMapping("/read-all")
  public ResponseEntity<Void> confirmAllNotifications(
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
  ){
    notificationService.confirmAllNotifications(requestUserId);
    return ResponseEntity.noContent().build();
  }

  // 알림 목록 조회
  @GetMapping
  public ResponseEntity<SliceCursorPageResponse<NotificationDto>> getNotifications(
      @RequestParam UUID userId,
      @RequestParam(required = false) Order direction,
      @RequestParam(required = false) UUID cursor,
      @RequestParam(required = false)Instant after,
      @RequestParam(required = false) Integer limit
  ){
    NotificationSearchRequest request = NotificationSearchRequest.builder()
        .userId(userId)
        .direction(direction)
        .cursor(cursor)
        .after(after)
        .limit(limit)
        .build();
    SliceCursorPageResponse<NotificationDto> response = notificationService.getNotifications(request);
    return ResponseEntity.ok(response);
  }
}
