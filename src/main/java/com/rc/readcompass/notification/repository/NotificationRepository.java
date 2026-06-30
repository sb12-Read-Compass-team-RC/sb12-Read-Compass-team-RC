package com.rc.readcompass.notification.repository;

import com.rc.readcompass.notification.entity.Notification;
import com.rc.readcompass.notification.repository.querydsl.NotificationQueryRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationQueryRepository {
}
