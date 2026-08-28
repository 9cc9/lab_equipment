package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResultDTO {
    private int totalRows;
    private int successCount;
    private int failCount;
    private int roomCount;
    private List<String> errors;
}
