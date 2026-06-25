package com.rc.readcompass.book.dto;

import com.rc.readcompass.book.BookCategory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookDto(
    UUID id,
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    BookCategory category,
    String categoryLabel,
    String thumbnailUrl,
    int reviewCount,
    double rating,
    Instant createdAt,
    Instant updatedAt
) {
}