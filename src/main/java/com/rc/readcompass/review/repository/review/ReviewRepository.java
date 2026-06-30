package com.rc.readcompass.review.repository.review;

import com.rc.readcompass.review.entity.Review;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {
    // 리뷰 단건 조회 - 논리 삭제 제외
    Optional<Review> findByIdAndDeletedFalse(UUID reviewId);

    // 업데이트 접근용 (충돌 안나게 lock을 걸어서 사용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @NotNull
    @Query("""
        select r
        from Review r
        where r.id = :reviewId
            and r.deleted = false
    """)
    Optional<Review> findActiveByIdForUpdate(UUID reviewId);

    // 논리 삭제된 리뷰제외 중복 체크
    boolean existsByBookIdAndUserIdAndDeletedFalse(UUID bookId, UUID userId);

    Optional<Review> findById(UUID reviewId);
}
