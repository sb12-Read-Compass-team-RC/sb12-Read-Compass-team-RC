package com.rc.readcompass.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest(
        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "평점은 필수 입니다.")
        @Min(1) @Max(5)
        Integer rating
) {
}
