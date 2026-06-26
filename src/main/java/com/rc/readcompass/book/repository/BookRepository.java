package com.rc.readcompass.book.repository;

import com.rc.readcompass.book.entity.Book;
import com.rc.readcompass.book.repository.querydsl.BookQueryRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookQueryRepository {

//  논리삭제가 기본 -> 조회/수정/삭제전 deleted 확인
  Optional<Book> findByIdAndDeletedFalse(UUID id);

  boolean existsByIsbn(String isbn);
}