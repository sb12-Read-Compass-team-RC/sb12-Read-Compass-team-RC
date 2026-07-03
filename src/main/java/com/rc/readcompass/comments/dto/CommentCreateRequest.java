package com.rc.readcompass.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 작성자(userId)는 요청 바디로 받지 않는다.
 * 인증된 사용자 정보(@AuthenticationPrincipal)에서 컨트롤러가 꺼내 서비스로 전달한다.
 */
public record CommentCreateRequest(
    @NotNull(message = "리뷰 ID는 필수입니다")
    UUID reviewId,

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "댓글은 500자 이하여야 합니다")
    String content
) {

}
