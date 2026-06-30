package com.rc.readcompass.notification.repository.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.notification.dto.NotificationDto;
import com.rc.readcompass.notification.dto.NotificationSearchRequest;
import com.rc.readcompass.notification.entity.QNotification;
import com.rc.readcompass.review.entity.QReview;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository{
  private final JPAQueryFactory queryFactory;

  private static final QNotification notification = QNotification.notification;
  private static final QReview review = QReview.review;

  @Override
  public SliceCursorPageResponse<NotificationDto> findNotificationsByUserId(
      NotificationSearchRequest req) {

    List<NotificationDto> notifications =
      queryFactory
          .select(
              Projections.constructor(
                  NotificationDto.class,
                  notification.id,
                  notification.user.id,
                  notification.review.id,
                  review.content,
                  notification.message,
                  notification.confirmed,
                  notification.createdAt,
                  notification.confirmedAt
              )
          )
          .from(notification)
          .join(notification.review, review)
          .where(
                  notification.user.id.eq(req.userId()),
                  cursorCondition(req)
          )
          .orderBy(orderBy(req))
          .limit(req.limit() + 1)
          .fetch();

    boolean hasNext = notifications.size() > req.limit();

    if (hasNext) {
      notifications.remove(req.limit().intValue());
    }

    NotificationDto last = notifications.isEmpty() ? null : notifications.get(notifications.size() - 1);

    String nextCursor = last == null ? null : last.id().toString();

    Instant nextAfter = last == null ? null : last.createdAt();

    long totalElements = countNotificationsByUserId(req.userId());

    return SliceCursorPageResponse.<NotificationDto>builder()
        .content(notifications)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(notifications.size())
        .totalElements(totalElements)
        .hasNext(hasNext)
        .build();
  }

  private BooleanExpression cursorCondition(NotificationSearchRequest req){

    if (req.after() == null || req.cursor() == null) {
      return null;
    }

    if (req.direction() == Order.ASC) {
      return notification.createdAt.gt(req.after())
          .or(
            notification.createdAt.eq(req.after())
                .and(notification.id.gt(req.cursor()))
          );
    }

    return notification.createdAt.lt(req.after())
        .or(
            notification.createdAt.eq(req.after())
                .and(notification.id.lt(req.cursor()))
        );
  }

  private OrderSpecifier<?>[] orderBy(NotificationSearchRequest req) {

    if (req.direction() == Order.ASC) {
      return new OrderSpecifier[]{
          notification.createdAt.asc(),
          notification.id.asc()
      };
    }
    return new OrderSpecifier[]{
      notification.createdAt.desc(),
      notification.id.desc()
    };
  }

  private long countNotificationsByUserId(UUID userId) {

    return Optional.ofNullable(
        queryFactory
            .select(notification.count())
            .from(notification)
            .where(
                notification.user.id.eq(userId)
            )
            .fetchOne()
    ).orElse(0L);
  }

}
