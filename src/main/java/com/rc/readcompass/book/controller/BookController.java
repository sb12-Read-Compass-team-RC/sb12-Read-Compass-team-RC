package com.rc.readcompass.book.controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.book.dto.BookCreateRequest;
import com.rc.readcompass.book.dto.BookDto;
import com.rc.readcompass.book.dto.BookSearchRequest;
import com.rc.readcompass.book.dto.BookUpdateRequest;
import com.rc.readcompass.book.dto.PopularBookDto;
import com.rc.readcompass.book.service.BookService;
import com.rc.readcompass.book.dto.NaverBookDto;

import com.rc.readcompass.book.service.PopularBookService;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;
  private final PopularBookService popularBookService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> createBook(
      @Valid @RequestPart("bookData")BookCreateRequest bookData,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    BookDto response = bookService.create(bookData, thumbnailImage);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/info")
  public ResponseEntity<NaverBookDto> getBookInfoByIsbn(
      @RequestParam String isbn
  ) {
    return ResponseEntity.ok(bookService.getBookInfoByIsbn(isbn));
  }

  @PostMapping(value="/isbn/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> extractIsbnFromImage(
      @RequestPart("image") MultipartFile image
  ) {
    return ResponseEntity.ok(bookService.extractIsbnFromImage(image));
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> getBook(@PathVariable UUID bookId) {
    return ResponseEntity.ok(bookService.findById(bookId));
  }

  @GetMapping
  public ResponseEntity<SliceCursorPageResponse<BookDto>> searchBooks(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "title") String orderBy,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(defaultValue = "50") Integer limit
  ) {
    BookSearchRequest request = BookSearchRequest.builder()
        .keyword(keyword)
        .sort(orderBy)
        .direction(parseDirection(direction))
        .cursor(cursor)
        .after(after)
        .limit(limit)
        .build();

    return ResponseEntity.ok(bookService.search(request));
  }

  @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> updateBook(
      @PathVariable UUID bookId,
      @Valid @RequestPart("bookData") BookUpdateRequest bookData,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    return ResponseEntity.ok(bookService.update(bookId, bookData, thumbnailImage));
  }

  // 소프트 딜리트 - deleted_at 업데이트, DB 삭제X
  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
    bookService.delete(bookId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{bookId}/hard")
  public ResponseEntity<Void> hardDeleteBook(@PathVariable UUID bookId) {
    bookService.hardDelete(bookId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/popular")
  public ResponseEntity<SliceCursorPageResponse<PopularBookDto>> getPopularBooks(
      @RequestParam PeriodType period,
      @RequestParam String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam Integer limit
  ) {
    return ResponseEntity.ok(
        popularBookService.getPopularBooks(
            period,
            parseDirection(direction),
            cursor,
            after,
            limit
        )
    );
  }

  private Order parseDirection(String direction) {
    try {
      return Order.valueOf(direction.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new CustomException(ErrorCode.INVALID_REQUEST)
          .addDetail("direction은 ASC 또는 DESC 만 가능합니다.");
    }
  }
}
