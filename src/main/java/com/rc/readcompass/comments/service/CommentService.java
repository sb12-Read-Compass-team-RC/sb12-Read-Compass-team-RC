package com.rc.readcompass.comments.service;

import com.rc.readcompass.comments.dto.CommentCreateRequest;
import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.entity.Comment;
import com.rc.readcompass.comments.mapper.CommentMapper;
import com.rc.readcompass.comments.repository.CommentRepository;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.repository.ReviewRepository;
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final CommentMapper commentMapper;

  @Transactional
  public CommentDto register(CommentCreateRequest request){

    Review review = reviewRepository.findByIdAndDeletedFalse(request.reviewId())
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    Comment comment = commentMapper.toEntity(request, review, user);

    commentRepository.save(comment);

    return commentMapper.toResponse(comment);
  }

}