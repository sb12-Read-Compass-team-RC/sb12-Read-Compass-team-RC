package com.rc.readcompass.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewCreateRequest(

        @NotNull(message = "도서 Id는 필수입니다.")
        UUID bookId,

        @NotNull(message = "사용자 Id는 필수입니다.")
        UUID userId,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "평점은 필수입니다.")
        @Min(1) @Max(5)
        Integer rating
) {
}
