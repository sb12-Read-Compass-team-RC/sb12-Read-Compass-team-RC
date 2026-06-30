package com.rc.readcompass.notification.repository.querydsl;

import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.notification.dto.NotificationDto;
import com.rc.readcompass.notification.dto.NotificationSearchRequest;

public interface NotificationQueryRepository {
  SliceCursorPageResponse<NotificationDto> findNotificationsByUserId(NotificationSearchRequest req);
}
