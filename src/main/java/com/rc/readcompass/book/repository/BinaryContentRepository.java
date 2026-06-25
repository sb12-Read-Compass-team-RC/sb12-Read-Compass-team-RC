package com.rc.readcompass.book.repository;

import com.rc.readcompass.book.BinaryContent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {

  Optional<BinaryContent> findByBookId(UUID bookId);

  void deleteByBookId(UUID bookId);
}