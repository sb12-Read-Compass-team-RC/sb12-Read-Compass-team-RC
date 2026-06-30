package com.rc.readcompass.comments.controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.comments.dto.CommentCreateRequest;
import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.dto.CommentSearchRequest;
import com.rc.readcompass.comments.dto.CommentUpdateRequest;
import com.rc.readcompass.comments.service.CommentService;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDto> register(
      @Valid @RequestBody CommentCreateRequest request
  ){
    CommentDto response = commentService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentDto> getComment(
      @PathVariable UUID commentId
  ){
    CommentDto response = commentService.getComment(commentId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<SliceCursorPageResponse<CommentDto>> getComments(
      @RequestParam UUID reviewId,
      @RequestParam(required = false) Order direction,
      @RequestParam(required = false) UUID cursor,
      @RequestParam(required = false) Instant after,
      @RequestParam(required = false) Integer limit
  ){
    CommentSearchRequest request = CommentSearchRequest.builder()
        .reviewId(reviewId)
        .direction(direction)
        .cursor(cursor)
        .after(after)
        .limit(limit)
        .build();
    SliceCursorPageResponse<CommentDto> response = commentService.getComments(request);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> update(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @Valid @RequestBody CommentUpdateRequest request
  ){
    CommentDto response = commentService.update(commentId, userId, request);
    return ResponseEntity.ok(response);
  }

}
