package com.example.project_aichat.dto.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;
    private boolean hasMore;

    public static <T> PageResponse<T> of(IPage<T> pageResult) {
        return new PageResponse<>(
                pageResult.getRecords(),
                pageResult.getTotal(),
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize(),
                pageResult.getCurrent() * pageResult.getSize() < pageResult.getTotal()
        );
    }

    public static <T> PageResponse<T> of(List<T> list, long total, int page, int size) {
        return new PageResponse<>(list, total, page, size, (long) page * size < total);
    }
}
