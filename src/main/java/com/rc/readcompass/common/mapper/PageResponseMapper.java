package com.rc.readcompass.common.mapper;

import com.rc.readcompass.common.slice.SimplePageResponse;
import com.rc.readcompass.common.slice.SlicePageResponse;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

@Mapper(componentModel = "spring")
public interface PageResponseMapper {

    // Page<Entity> -> SimplePageResponse<Entity>
    default  <T> SimplePageResponse<T> toResponse(Page<T> page) {
        return SimplePageResponse.<T>builder()
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .items(page.getContent())
                .build();
    }


    // Page<Entity> -> SimplePageResponse<Dto>
    default  <T, R> SimplePageResponse<R> toResponse(Page<T> page, List<R> items) {
        return SimplePageResponse.<R>builder()
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .items(items)
                .build();
    }

    // Slice -> SlicePageResponse (items가 이미 Slice 내부에 있는 경우)
    default <T> SlicePageResponse<T> toSlice(Slice<T> slice) {
        return SlicePageResponse.<T>builder()
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .nextCursor(null)
                .items(slice.getContent())
                .build();
    }

    // Slice -> SlicePageResponse
    default <T> SlicePageResponse<T> toSlice(Slice<T> slice, Object nextCursor) {
        return SlicePageResponse.<T>builder()
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .nextCursor(nextCursor)
                .items(slice.getContent())
                .build();
    }

    // Slice -> SlicePageResponse
    default <T, R> SlicePageResponse<R> toSlice(Slice<T> slice, List<R> items, Object nextCursor) {
        return SlicePageResponse.<R>builder()
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .nextCursor(nextCursor)
                .items(items)
                .build();
    }

    // [프로젝트 대비] Slice<T> → SlicePageResponse<T>
    // nextAfter(Instant) 포함 버전 - PostFlatDetailDto처럼 createdAt이 있는 DTO에서 활용
    default <T> SlicePageResponse<T> toSliceWithAfter(Slice<T> slice) {
        Instant nextAfter = null;
        if (slice.hasNext() && !slice.isEmpty()) {
            try {
                Object last = slice.getContent().get(slice.getContent().size() - 1);
                var method = last.getClass().getMethod("createdAt");
                Object result = method.invoke(last);
                if (result instanceof Instant instant) nextAfter = instant;
            } catch (Exception ignored) {}
        }
        String nextCursor = slice.hasNext()
                ? String.valueOf(slice.getNumber() + 1) : null;

        return SlicePageResponse.<T>builder()
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .nextCursor(nextCursor)
                .items(slice.getContent())
                .build();
    }
}