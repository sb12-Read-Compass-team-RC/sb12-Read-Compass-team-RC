package com.rc.readcompass.book.entity;

import com.rc.readcompass.common.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "tb_books")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseUpdatableEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publishedDate;

    // ISBN은 등록 후 수정 불가
    @Column(nullable = false, updatable = false, length = 20, unique = true)
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "book_category", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private BookCategory category;

    // 리뷰 등록/삭제 시 갱신되는 비정규화 카운터
    @Builder.Default
    @Column(name = "review_cnt", nullable = false)
    private int reviewCnt = 0;

    // 리뷰 평점 평균: 리뷰 등록/삭제/수정 시 갱신
    @Builder.Default
    @Column(nullable = false)
    private double rating = 0.0;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // =====================================================
    // 도메인 메서드
    // =====================================================

    public void updateInfo(String title, String author, String description,
        String publisher, LocalDate publishedDate, BookCategory category) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.category = category;
    }

    /**
     * 리뷰 등록 시 호출.
     * 카운터 증가 후 평점 평균을 새로운 평점으로 재계산.
     */
    public void addReview(int newRating) {
        double totalRating = this.rating * this.reviewCnt + newRating;
        this.reviewCnt++;
        this.rating = totalRating / this.reviewCnt;
    }

    /**
     * 리뷰 삭제 시 호출 (논리/물리 삭제 공통).
     */
    public void removeReview(int removedRating) {
        if (this.reviewCnt <= 0) return;
        if (this.reviewCnt == 1) {
            this.reviewCnt = 0;
            this.rating = 0.0;
        } else {
            double totalRating = this.rating * this.reviewCnt - removedRating;
            this.reviewCnt--;
            this.rating = totalRating / this.reviewCnt;
        }
    }

    /**
     * 리뷰 수정 시 호출.
     */
    public void updateRating(int oldRating, int newRating) {
        if (this.reviewCnt <= 0) return;
        double totalRating = this.rating * this.reviewCnt - oldRating + newRating;
        this.rating = totalRating / this.reviewCnt;
    }

    public void softDelete() {
        this.deleted = true;
    }
}
