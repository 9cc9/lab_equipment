package org.uestc.weglas.biz.dto;

import lombok.Data;

@Data
public class UpdateEquipmentBorrowRequest {
    private Boolean borrowable;
    private String allowedBorrowTypes;
}
