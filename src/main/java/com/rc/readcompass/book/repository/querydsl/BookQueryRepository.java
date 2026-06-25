package com.rc.readcompass.book.repository.querydsl;

import com.rc.readcompass.book.dto.BookDto;
import com.rc.readcompass.book.dto.BookSearchRequest;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;

public interface BookQueryRepository {

  SliceCursorPageResponse<BookDto> searchCursor(BookSearchRequest request);
}