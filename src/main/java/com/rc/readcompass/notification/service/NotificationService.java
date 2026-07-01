package com.rc.readcompass.notification.service;

import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.notification.dto.NotificationDto;
import com.rc.readcompass.notification.dto.NotificationSearchRequest;
import com.rc.readcompass.notification.entity.Notification;
import com.rc.readcompass.notification.entity.NotificationType;
import com.rc.readcompass.notification.mapper.NotificationMapper;
import com.rc.readcompass.notification.repository.NotificationRepository;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.user.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  // 알림 읽음 상태 업데이트
  @Transactional
  public NotificationDto confirmNotification(
      UUID notificationId,
      UUID requestUserId
  ){
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() ->  new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    if (!notification.getUser().getId().equals(requestUserId)) {
      throw new CustomException(ErrorCode.NOTIFICATION_FORBIDDEN);
    }
    notification.confirm();
    return notificationMapper.toResponse(notification);
  }

  // 모든 알림 읽음 처리
  @Transactional
  public void confirmAllNotifications(UUID requestUserId){
    List<Notification> notifications =  notificationRepository.findByUserIdAndConfirmedFalse(requestUserId);
    for (Notification notification : notifications) {
      notification.confirm();
    }
  }

  // 알림 목록 조회
  @Transactional(readOnly = true)
  public SliceCursorPageResponse<NotificationDto> getNotifications(
      NotificationSearchRequest req
  ){
    return notificationRepository.findNotificationsByUserId(req);
  }

  // 리뷰에 좋아요
  @Transactional
  public void createLikeNotification(
      Review review,    // 어떤 리뷰인지
      User actor        // 좋아요를 누른 사용자
  ){
    User receiver = review.getUser(); // 알림을 받을 사람(리뷰 작성자)
    if (receiver.getId().equals(actor.getId())) {
      return;
    }
    String message = actor.getNickname() + "님이 나의 리뷰를 좋아합니다.";
    Notification notification = Notification.create(
        receiver,
        review,
        message,
        NotificationType.REVIEW_LIKE
    );
    notificationRepository.save(notification);
  }

  // 리뷰에 댓글
  @Transactional
  public void createCommentNotification(
      Review review,    // 댓글이 달린 리뷰
      User actor        // 댓글을 작성한 사용자
  ){
    User receiver = review.getUser(); // 알림을 받을 사람(리뷰 작성자)
    if (receiver.getId().equals(actor.getId())) {
      return;
    }
    String message = actor.getNickname() + "님이 나의 리뷰에 댓글을 남겼습니다.";
    Notification notification = Notification.create(
        receiver,
        review,
        message,
        NotificationType.REVIEW_COMMENT
    );
    notificationRepository.save(notification);
  }

}
