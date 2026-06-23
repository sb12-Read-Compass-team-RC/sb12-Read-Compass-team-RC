package com.rc.readcompass.common.slice;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SimplePageResponse<T> {
    private int page;
    private int size;
    private int totalPages;
    private long totalItems;
    private List<T> items;
}