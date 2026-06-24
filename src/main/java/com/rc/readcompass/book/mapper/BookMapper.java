package com.rc.readcompass.book.mapper;

import com.rc.readcompass.book.Book;
import com.rc.readcompass.book.dto.BookCreateRequest;
import com.rc.readcompass.book.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "reviewCnt", ignore = true)
  @Mapping(target = "rating", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Book toEntity(BookCreateRequest request);

  @Mapping(target = "thumbnailUrl", source = "thumbnailUrl")
  @Mapping(target = "reviewCount", source = "book.reviewCnt")
  @Mapping(target = "categoryLabel", expression = "java(book.getCategory().getLabel())")
  BookDto toDto(Book book, String thumbnailUrl);
}