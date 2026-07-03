package com.rc.readcompass.book.service;

import com.rc.readcompass.book.entity.BinaryContent;
import com.rc.readcompass.book.entity.Book;
import com.rc.readcompass.book.dto.BookCreateRequest;
import com.rc.readcompass.book.dto.BookDto;
import com.rc.readcompass.book.dto.BookSearchRequest;
import com.rc.readcompass.book.dto.BookUpdateRequest;
import com.rc.readcompass.book.mapper.BookMapper;
import com.rc.readcompass.book.repository.BinaryContentRepository;
import com.rc.readcompass.book.repository.BookRepository;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.storage.FileStorage;
import com.rc.readcompass.book.client.NaverBookClient;
import com.rc.readcompass.book.dto.NaverBookDto;
import com.rc.readcompass.book.client.OcrSpaceClient;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BookMapper bookMapper;
  private final FileStorage fileStorage;
  private final NaverBookClient naverBookClient;
  private final OcrSpaceClient ocrSpaceClient;

  @Transactional
  public BookDto create(BookCreateRequest request, MultipartFile thumbnail) {
    if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
      throw new CustomException(ErrorCode.DUPLICATE_ISBN)
          .addDetail("isbn=" + request.isbn());
    }

    Book book = bookMapper.toEntity(request);
    Book savedBook = bookRepository.save(book);

    BinaryContent savedThumbnail = saveThumbnail(savedBook, thumbnail);

    if (savedThumbnail == null) {
      savedThumbnail = saveThumbnailFromUrl(savedBook, request.thumbnailUrl());
    }

    return bookMapper.toDto(
        savedBook,
        getThumbnailUrl(savedThumbnail),
        savedBook.getReviewCnt(),
        savedBook.getRating()
    );
  }

  @Transactional(readOnly = true)
  public BookDto findById(UUID id) {
    Book book = findActiveBook(id);

    String thumbnailUrl = binaryContentRepository.findByBookId(id)
        .map(this::getThumbnailUrl)
        .orElse(null);

    return bookMapper.toDto(
        book,
        thumbnailUrl,
        book.getReviewCnt(),
        book.getRating()
    );
  }

  @Transactional(readOnly = true)
  public SliceCursorPageResponse<BookDto> search(BookSearchRequest request) {
    SliceCursorPageResponse<BookDto> response = bookRepository.searchCursor(request);

    List<BookDto> content = response.getContent().stream()
        .map(this::convertThumbnailFileNameToUrl)
        .toList();

    return SliceCursorPageResponse.<BookDto>builder()
        .content(content)
        .hasNext(response.isHasNext())
        .size(response.getSize())
        .nextCursor(response.getNextCursor())
        .nextAfter(response.getNextAfter())
        .totalElements(response.getTotalElements())
        .build();
  }

  @Transactional
  public BookDto update(UUID id, BookUpdateRequest request, MultipartFile thumbnail) {
    Book book = findActiveBook(id);

    book.updateInfo(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.category()
    );

    if (hasFile(thumbnail)) {
      replaceThumbnail(book, thumbnail);
    } else if (Boolean.TRUE.equals(request.thumbnailDeleted())) {
      deleteThumbnail(book);
    }

    String thumbnailUrl = binaryContentRepository.findByBookId(id)
        .map(this::getThumbnailUrl)
        .orElse(null);

    return bookMapper.toDto(
        book,
        thumbnailUrl,
        book.getReviewCnt(),
        book.getRating()
    );
  }

  @Transactional
  public void delete(UUID id) {
    Book book = findActiveBook(id);
    book.softDelete();
  }

  @Transactional
  public void hardDelete(UUID id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND)
            .addDetail("bookId=" + id));

    binaryContentRepository.findByBookId(id)
        .ifPresent(binaryContent -> {
          fileStorage.delete(List.of(binaryContent));
          binaryContentRepository.delete(binaryContent);
        });

    bookRepository.delete(book);
  }

  @Transactional(readOnly = true)
  public String extractIsbnFromImage(MultipartFile image) {
    String parsedText = ocrSpaceClient.extractText(image);
    return extractIsbn(parsedText);
  }

  @Transactional(readOnly = true)
  public NaverBookDto getBookInfoByIsbn(String isbn) {
    if (isbn == null || isbn.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_REQUEST)
          .addDetail("isbn은 필수입니다.");
    }

    return naverBookClient.searchByIsbn(isbn.trim());
  }

  private Book findActiveBook(UUID id) {
    return bookRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND)
            .addDetail("bookId=" + id));
  }

  private BinaryContent saveThumbnail(Book book, MultipartFile thumbnail) {
    if (!hasFile(thumbnail)) {
      return null;
    }

    List<BinaryContent> savedFiles = fileStorage.save(List.of(thumbnail));

    if (savedFiles.isEmpty()) {
      return null;
    }

    BinaryContent binaryContent = savedFiles.get(0);
    binaryContent.assignBook(book);

    return binaryContentRepository.save(binaryContent);
  }

  private BinaryContent saveThumbnailFromUrl(Book book, String thumbnailUrl) {
    if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
      return null;
    }

    try {
      MultipartFile file = downloadImageAsMultipartFile(thumbnailUrl);
      return saveThumbnail(book, file);
    } catch (Exception e) {
      return null;
    }
  }

  private MultipartFile downloadImageAsMultipartFile(String imageUrl) throws IOException {
    HttpURLConnection con = (HttpURLConnection) URI.create(imageUrl).toURL().openConnection();
    con.setRequestMethod("GET");
    con.setConnectTimeout(3000);
    con.setReadTimeout(5000);

    int responseCode = con.getResponseCode();

    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("이미지 다운로드 실패");
    }

    String contentType = con.getContentType();
    byte[] bytes;

    try (InputStream inputStream = con.getInputStream()) {
      bytes = inputStream.readAllBytes();
    } finally {
      con.disconnect();
    }

    String filename = createThumbnailFileName(imageUrl, contentType);

    return new UrlImageMultipartFile(
        "thumbnailImage",
        filename,
        contentType,
        bytes
    );
  }

  private String createThumbnailFileName(String imageUrl, String contentType) {
    String extension = ".jpg";

    if (contentType != null) {
      if (contentType.contains("png")) {
        extension = ".png";
      } else if (contentType.contains("webp")) {
        extension = ".webp";
      } else if (contentType.contains("gif")) {
        extension = ".gif";
      }
    }

    return "naver-book-cover" + extension;
  }

  private void replaceThumbnail(Book book, MultipartFile thumbnail) {
    BinaryContent oldThumbnail = binaryContentRepository.findByBookId(book.getId())
        .orElse(null);

    List<BinaryContent> savedFiles = fileStorage.save(List.of(thumbnail));

    if (savedFiles.isEmpty()) {
      return;
    }

    BinaryContent newThumbnail = savedFiles.get(0);

    if (oldThumbnail == null) {
      newThumbnail.assignBook(book);
      binaryContentRepository.save(newThumbnail);
    } else {
      String oldRenamedFileUrl = oldThumbnail.getRenamedFileUrl();
      String oldOriginFileUrl = oldThumbnail.getOriginFileUrl();
      Long oldSize = oldThumbnail.getSize();
      String oldContentType = oldThumbnail.getContentType();

      oldThumbnail.updateFileInfo(newThumbnail);

      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          BinaryContent oldFile = BinaryContent.builder()
              .book(book)
              .originFileUrl(oldOriginFileUrl)
              .renamedFileUrl(oldRenamedFileUrl)
              .size(oldSize)
              .contentType(oldContentType)
              .build();

          fileStorage.delete(List.of(oldFile));
        }

        @Override
        public void afterCompletion(int status) {
          if (status == STATUS_ROLLED_BACK) {
            fileStorage.delete(List.of(newThumbnail));
          }
        }
      });
    }
  }

  private boolean hasFile(MultipartFile file) {
    return file != null && !file.isEmpty();
  }

  private String getThumbnailUrl(BinaryContent binaryContent) {
    if (binaryContent == null || binaryContent.getRenamedFileUrl() == null) {
      return null;
    }

    return fileStorage.getAttachFileUrl(binaryContent.getRenamedFileUrl());
  }

  private BookDto convertThumbnailFileNameToUrl(BookDto dto) {
    String thumbnailUrl = dto.thumbnailUrl();

    if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
      thumbnailUrl = fileStorage.getAttachFileUrl(thumbnailUrl);
    }

    return new BookDto(
        dto.id(),
        dto.title(),
        dto.author(),
        dto.description(),
        dto.publisher(),
        dto.publishedDate(),
        dto.isbn(),
        dto.category(),
        dto.categoryLabel(),
        thumbnailUrl,
        dto.reviewCount(),
        dto.rating(),
        dto.createdAt(),
        dto.updatedAt()
    );
  }

  private void deleteThumbnail(Book book) {
    BinaryContent thumbnail = binaryContentRepository.findByBookId(book.getId())
        .orElse(null);

    if (thumbnail == null) {
      return;
    }

    binaryContentRepository.delete(thumbnail);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        fileStorage.delete(List.of(thumbnail));
      }
    });
  }

  private String extractIsbn(String parsedText) {
    Pattern isbnPattern = Pattern.compile(
        "ISBN\\s*([0-9Xx][-\\s]?[0-9Xx][-\\s]?[0-9Xx][-\\s]?[0-9Xx][-\\s]?[0-9Xx][-\\s]?"
            + "[0-9Xx][-\\s]?[0-9Xx][-\\s]?[0-9Xx][-\\s]?[0-9Xx][-\\s]?[0-9Xx][-\\s]?"
            + "[0-9Xx]?[-\\s]?[0-9Xx]?[-\\s]?[0-9Xx]?)",
        Pattern.CASE_INSENSITIVE
    );

    Matcher matcher = isbnPattern.matcher(parsedText);

    if (matcher.find()) {
      String isbn = matcher.group(1).replaceAll("[^0-9Xx]", "");

      if (isbn.length() == 10 || isbn.length() == 13) {
        return isbn;
      }
    }

    Pattern fallbackPattern = Pattern.compile("(97[89][0-9\\s-]{10,20})");
    Matcher fallbackMatcher = fallbackPattern.matcher(parsedText);

    if (fallbackMatcher.find()) {
      String isbn = fallbackMatcher.group(1).replaceAll("[^0-9]", "");

      if (isbn.length() == 13) {
        return isbn;
      }
    }

    throw new CustomException(ErrorCode.INVALID_REQUEST)
        .addDetail("OCR 결과에서 ISBN을 찾을 수 없습니다.");
  }

  private static class UrlImageMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    private UrlImageMultipartFile(
        String name,
        String originalFilename,
        String contentType,
        byte[] content
    ) {
      this.name = name;
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.content = content;
    }



    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getOriginalFilename() {
      return originalFilename;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public boolean isEmpty() {
      return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
      return content.length;
    }

    @Override
    public byte[] getBytes() {
      return content;
    }

    @Override
    public InputStream getInputStream() {
      return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
      Files.write(dest.toPath(), content);
    }
  }
}