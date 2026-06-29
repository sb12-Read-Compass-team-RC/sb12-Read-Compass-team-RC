package com.rc.readcompass.notification.mapper;

import com.rc.readcompass.notification.dto.NotificationDto;
import com.rc.readcompass.notification.entity.Notification;
import com.rc.readcompass.notification.entity.NotificationType;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "confirmedAt", ignore = true)
  @Mapping(target = "review", source = "review")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "message", source = "message")
  @Mapping(target = "notiType", source = "notiType")
  Notification toEntity(
      Review review,
      User user,
      String message,
      NotificationType notiType
  );

  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "reviewContent", source = "review.content")
  @Mapping(target = "userId", source = "user.id")
  NotificationDto toResponse(Notification notification);

}
