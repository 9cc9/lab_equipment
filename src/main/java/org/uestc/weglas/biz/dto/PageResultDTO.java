package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResultDTO<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
}
