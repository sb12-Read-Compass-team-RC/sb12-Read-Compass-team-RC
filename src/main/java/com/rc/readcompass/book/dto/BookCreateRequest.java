package com.rc.readcompass.book.dto;

import com.rc.readcompass.book.entity.BookCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookCreateRequest(
    @NotBlank(message = "도서 제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    String title,

    @NotBlank(message = "저자는 필수입니다.")
    @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
    String author,

    @NotBlank(message = "도서 설명은 필수입니다.")
    String description,

    @NotBlank(message = "출판사는 필수입니다.")
    @Size(max = 100, message = "출판사는 100자 이하여야 합니다.")
    String publisher,

    @NotNull(message = "출판일은 필수입니다.")
    LocalDate publishedDate,

    @NotBlank(message = "ISBN은 필수입니다.")
    @Size(max = 20, message = "ISBN은 20자 이하여야 합니다.")
    String isbn,

    @NotNull(message = "도서 카테고리는 필수입니다.")
    BookCategory category,

    String thumbnailUrl
) {
}