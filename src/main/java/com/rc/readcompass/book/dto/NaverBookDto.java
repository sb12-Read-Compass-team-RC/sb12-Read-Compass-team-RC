package com.rc.readcompass.book.dto;

import java.time.LocalDate;

public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,

    byte[] thumbnailImage
    /*
      Naver image URL
        → 이미지 다운로드
        → byte[] 변환
        → thumbnailImage에 담기
     */
) {
}