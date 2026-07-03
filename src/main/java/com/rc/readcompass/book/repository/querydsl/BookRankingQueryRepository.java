package com.rc.readcompass.book.repository.querydsl;

import com.querydsl.core.types.Order;
import com.rc.readcompass.book.dto.PopularBookDto;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import java.time.Instant;

public interface BookRankingQueryRepository {

    SliceCursorPageResponse<PopularBookDto> searchCursor(
        PeriodType periodType,
        Order direction,
        String cursor,
        Instant after,
        int limit
    );
}
