package com.rc.readcompass.book.repository;

import com.rc.readcompass.book.entity.BookRanking;
import com.rc.readcompass.book.repository.querydsl.BookRankingQueryRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRankingRepository
    extends JpaRepository<BookRanking, UUID>, BookRankingQueryRepository {
}
