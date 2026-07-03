package com.rc.readcompass.book.service;

import com.querydsl.core.types.Order;
import com.rc.readcompass.book.dto.PopularBookDto;
import com.rc.readcompass.book.repository.BookRankingRepository;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.storage.FileStorage;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PopularBookService {

    private final BookRankingRepository bookRankingRepository;
    private final FileStorage           fileStorage;

    @Transactional(readOnly = true)
    public SliceCursorPageResponse<PopularBookDto> getPopularBooks(
        PeriodType periodType,
        Order direction,
        String cursor,
        Instant after,
        int limit
    ) {
        SliceCursorPageResponse<PopularBookDto> response =
            bookRankingRepository.searchCursor(periodType, direction, cursor, after, limit);

        List<PopularBookDto> content = response.getContent().stream()
            .map(this::convertThumbnailUrl)
            .toList();

        return SliceCursorPageResponse.<PopularBookDto>builder()
            .content(content)
            .hasNext(response.isHasNext())
            .size(response.getSize())
            .nextCursor(response.getNextCursor())
            .nextAfter(response.getNextAfter())
            .totalElements(response.getTotalElements())
            .build();
    }

    private PopularBookDto convertThumbnailUrl(PopularBookDto dto) {
        String url = dto.thumbnailUrl();
        if (url != null && !url.isBlank()) {
            url = fileStorage.getAttachFileUrl(url);
        }
        return new PopularBookDto(
            dto.id(),
            dto.bookId(),
            dto.title(),
            dto.author(),
            url,
            dto.period(),
            dto.rank(),
            dto.score(),
            dto.reviewCount(),
            dto.rating(),
            dto.createdAt()
        );
    }
}