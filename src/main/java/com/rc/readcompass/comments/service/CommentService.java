package com.rc.readcompass.comments.service;

import com.rc.readcompass.comments.dto.CommentCreateRequest;
import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.dto.CommentSearchRequest;
import com.rc.readcompass.comments.dto.CommentUpdateRequest;
import com.rc.readcompass.comments.entity.Comment;
import com.rc.readcompass.comments.mapper.CommentMapper;
import com.rc.readcompass.comments.repository.CommentRepository;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.review.entity.Review;
import com.rc.readcompass.review.repository.ReviewRepository;
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRepository;
import java.util.UUID;
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

  @Transactional(readOnly = true)
  public CommentDto getComment(
      UUID commentId
  ){
    Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    return commentMapper.toResponse(comment);
  }

  @Transactional(readOnly = true)
  public SliceCursorPageResponse<CommentDto> getComments(
      CommentSearchRequest req
  ){
    reviewRepository.findByIdAndDeletedFalse(req.reviewId())
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    return commentRepository.findCommentsByReviewId(req);
  }

  @Transactional
  public CommentDto update(
      UUID commentId,
      UUID userId,
      CommentUpdateRequest request
  ){
    Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.getUser().getId().equals(userId)){
      throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
    }
    comment.updateContent(request.content());
    return commentMapper.toResponse(comment);
  }

  @Transactional
  public void delete(
      UUID commentId,
      UUID userId
  ){
    Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.getUser().getId().equals(userId)){
      throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
    }
    comment.softDelete();
  }

  @Transactional
  public void hardDelete(
      UUID commentId,
      UUID userId
  ){
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.getUser().getId().equals(userId)){
      throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
    }
    commentRepository.delete(comment);
  }

}