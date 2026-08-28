package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchUpdateResultDTO {
    private int totalCount;
    private int successCount;
    private int failCount;
    private List<String> errors;
}
