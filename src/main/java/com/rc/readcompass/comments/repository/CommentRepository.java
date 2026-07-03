package com.rc.readcompass.comments.repository;

import com.rc.readcompass.comments.entity.Comment;
import com.rc.readcompass.comments.repository.querydsl.CommentQueryRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentQueryRepository {

  Optional<Comment> findByIdAndDeletedFalse(UUID id);

  void deleteAllByReviewId(UUID reviewId);

}
