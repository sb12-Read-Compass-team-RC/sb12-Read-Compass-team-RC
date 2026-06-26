package com.rc.readcompass.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.review.dto.ReviewDto;
import com.rc.readcompass.review.dto.ReviewSearchRequest;
import com.rc.readcompass.review.entity.QReview;
import com.rc.readcompass.review.entity.QReviewLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReview r = QReview.review;
    private final QReviewLike myLike = new QReviewLike("myLike");

    @Override
    public SliceCursorPageResponse<ReviewDto> searchCursorSortedFlat(ReviewSearchRequest request) {
        BooleanBuilder where = new BooleanBuilder();

        // soft delete 제외
        where.and(r.deleted.isFalse());

        // 동적 쿼리 검색
        // 부분일치 : 작성자 닉네임, 내용, 도서제목
        // 완전 일치 : 작성자ID, 도서 ID, 조회 조건이 여러개인 경우 전부 만족한 결과로 조회
        if(request.keyword() !=  null && !request.keyword().isBlank()) {
            String keyword = request.keyword();

            where.and(
                    r.user.nickname.containsIgnoreCase(keyword)
                    .or(r.content.containsIgnoreCase(keyword))
                    .or(r.content.containsIgnoreCase(keyword))
            );
        }

        if(request.userId() != null){
            where.and(r.user.id.eq(request.userId()));
        }

        if(request.bookId() != null){
            where.and(r.book.id.eq(request.bookId()));
        }

        // 정렬 방향
        Order order = (request.direction() == null || request.direction() == Order.DESC)
                ? Order.DESC : Order.ASC;

        // 정렬 기준
        String orderBy = (request.orderBy() == null || request.orderBy().isBlank())
                ?"createdAt"
                : request.orderBy();

        // cursor = 이전 응답의 nextCursor, 첫페이지면 null

        String cursor = request.cursor();
        Instant after = request.after();

        OrderSpecifier<?>[] orderSpecifiers;

        // 정렬 기준 화이트리스트 + 커서 조건
        if("rating".equals(orderBy)){
            orderSpecifiers = new OrderSpecifier[]{
                    new OrderSpecifier<>(order, r.rating),
                    new OrderSpecifier<>(order,r.createdAt)
            };

            // rating 정렬 :
            // cursor = 이전 페이지 마지막 요소의 rating
            // after =  이전 페이지 마지막 요소의 createdAt
            if(cursor != null && !cursor.isBlank() && after != null){
                int cursorRating = Integer.parseInt(cursor);

                if(order == Order.DESC){
                    where.and(r.rating.lt(cursorRating)
                        .or(r.rating.eq(cursorRating)
                        .and(r.createdAt.lt(after))
                        )
                    );
                } else {
                    where.and(r.rating.gt(cursorRating)
                            .or(r.rating.eq(cursorRating)
                                    .and(r.createdAt.gt(after))
                            )
                    );
                }
            }
        } else if ("createdAt".equals(orderBy)){
            orderSpecifiers = new OrderSpecifier[]{
                    new OrderSpecifier<>(order, r.createdAt)
            };

            if(cursor != null && !cursor.isBlank()){
                Instant cursorCreatedAt = Instant.parse(cursor);

                if(order == Order.DESC){
                    where.and(r.createdAt.lt(cursorCreatedAt));
                } else{
                    where.and(r.createdAt.gt(cursorCreatedAt));
                }
            }

        } else{
            throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + orderBy);
        }

        Integer size =(request.limit() == null || request.limit() > 0)
                ? request.limit()
                : Integer.valueOf(50);

        // likedByMe처리
        // request.requestUserId가 null이면 false
        // controller / service에서 필수 검증 실시

        BooleanExpression likedByMeExpression = myLike.id.isNotNull();

        List<ReviewDto> rowsPlusOne = queryFactory
                .select(Projections.constructor(
                        ReviewDto.class,
                        r.id,
                        r.book.id,
                        r.book.title,

//                      썸네일 조인 구조에 맞게 교체  binaryContent.renamedFileUrl,
                        Expressions.nullExpression(String.class),

                        r.user.id,
                        r.user.nickname,
                        r.content,
                        r.rating,
                        r.likeCnt.longValue(),
                        r.commentCnt.longValue(),
                        likedByMeExpression,
                        r.createdAt,
                        r.updatedAt
                ))
                .from(r)
                .join(r.user)
                .join(r.book)
                .leftJoin(myLike)
                    .on(myLike.review.eq(r)
                            .and(myLike.user.id.eq(request.requestUserId()))
                    )
                .where(where)
                .orderBy(orderSpecifiers)
                .limit(size+1L)
                .fetch();

        boolean hasNext = rowsPlusOne.size() > size;
        List<ReviewDto> contents = hasNext ?
                rowsPlusOne.subList(0,size)
                : rowsPlusOne;

        String nextCursor = null;
        Instant  nextAfter = null;

        if(!contents.isEmpty()){
            ReviewDto last = contents.get(contents.size() - 1);

            if("rating".equals(orderBy)){
                nextCursor = String.valueOf(last.rating());

                nextAfter = last.createdAt();
            } else {
                nextCursor = last.createdAt().toString();

                nextAfter = last.createdAt();
            }
        }

        return SliceCursorPageResponse.<ReviewDto>builder()
                .content(contents)
                .hasNext(hasNext)
                .size(size)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .totalElements(-1L)
                .build();
    }
}
