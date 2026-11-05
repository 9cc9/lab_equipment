package org.uestc.weglas.biz.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateEquipmentBorrowRequest {
    private List<String> assetCodes;
    private Boolean borrowable;
    private String allowedBorrowTypes;
}
