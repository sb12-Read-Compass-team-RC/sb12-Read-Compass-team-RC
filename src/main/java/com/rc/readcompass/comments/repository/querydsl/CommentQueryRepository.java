package com.rc.readcompass.comments.repository.querydsl;

import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.dto.CommentSearchRequest;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;

public interface CommentQueryRepository {
  SliceCursorPageResponse<CommentDto> findCommentsByReviewId(CommentSearchRequest req);
}
