package com.rc.readcompass.notification.mapper;

import com.rc.readcompass.notification.dto.NotificationDto;
import com.rc.readcompass.notification.entity.Notification;
import com.rc.readcompass.notification.entity.NotificationType;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.user.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "user", source = "user")
  @Mapping(target = "review", source = "review")
  @Mapping(target = "message", source = "message")
  @Mapping(target = "notiType", source = "notiType")
  Notification toEntity(
      User user,
      Review review,
      String message,
      NotificationType notiType
  );

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "reviewContent", source = "review.content")
  NotificationDto toResponse(Notification notification);

  List<NotificationDto> toResponseList(List<Notification> notifications);


}
