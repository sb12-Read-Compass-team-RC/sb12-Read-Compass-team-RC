package com.rc.readcompass.comments.controller;

import com.rc.readcompass.comments.dto.CommentCreateRequest;
import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
