package com.rc.readcompass.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 작성자(userId)는 요청 바디로 받지 않는다.
 * 인증된 사용자 정보(@AuthenticationPrincipal)에서 컨트롤러가 꺼내 서비스로 전달한다.
 */
public record ReviewCreateRequest(

        @NotNull(message = "도서 Id는 필수입니다.")
        UUID bookId,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "평점은 필수입니다.")
        @Min(1) @Max(5)
        Integer rating
) {
}
