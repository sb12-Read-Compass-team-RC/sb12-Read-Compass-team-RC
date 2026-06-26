package com.rc.readcompass.comments.repository.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.dto.CommentSearchRequest;
import com.rc.readcompass.comments.entity.QComment;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.user.QUser;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository{

  private final JPAQueryFactory queryFactory;

  private static final QComment comment = QComment.comment;
  private static final QUser user = QUser.user;

  @Override
  public SliceCursorPageResponse<CommentDto> findCommentsByReviewId(
      CommentSearchRequest req) {

    List<CommentDto> comments =
      queryFactory
          .select(
              Projections.constructor(
                  CommentDto.class,
                  comment.id,
                  comment.review.id,
                  comment.user.id,
                  user.nickname,
                  comment.content,
                  comment.createdAt,
                  comment.updatedAt
              )
          )
          .from(comment)
          .join(comment.user, user)
          .where(
             comment.review.id.eq(req.reviewId()),
             comment.deleted.isFalse(),
             cursorCondition(req)
          )
          .orderBy(orderBy(req))
          .limit(req.limit() + 1)
          .fetch();

    boolean hasNext = comments.size() > req.limit();

    if (hasNext) {
      comments.remove(req.limit().intValue());
    }

    CommentDto last = comments.isEmpty() ? null : comments.get(comments.size() - 1);

    String nextCursor = last == null ? null : last.id().toString();

    Instant nextAfter = last == null ? null : last.createdAt();

    long totalElements = countByReview(req.reviewId());

    return SliceCursorPageResponse.<CommentDto>builder()
        .content(comments)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(comments.size())
        .totalElements(totalElements)
        .hasNext(hasNext)
        .build();
  }

  private BooleanExpression cursorCondition(CommentSearchRequest req) {

    if (req.after() == null || req.cursor() == null) {
      return null;
    }

    if (req.direction() == Order.ASC) {
      return comment.createdAt.gt(req.after())
          .or(
              comment.createdAt.eq(req.after())
                  .and(comment.id.gt(req.cursor()))
          );
    }

    return comment.createdAt.lt(req.after())
        .or(
            comment.createdAt.eq(req.after())
                  .and(comment.id.lt(req.cursor()))
        );
  }

  private OrderSpecifier<?>[] orderBy(CommentSearchRequest req) {

    if (req.direction() == Order.ASC) {
      return new OrderSpecifier[]{
          comment.createdAt.asc(),
          comment.id.asc()
      };
    }
    return new OrderSpecifier[]{
        comment.createdAt.desc(),
        comment.id.desc()
    };
  }

  private long countByReview(UUID reviewId) {

    return Optional.ofNullable(
        queryFactory
            .select(comment.count())
            .from(comment)
            .where(
                comment.review.id.eq(reviewId),
                comment.deleted.isFalse()
            )
            .fetchOne()
    ).orElse(0L);
  }

}
