package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class EquipmentChangeLogDTO {
    private String id;
    private String assetCode;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String operatorName;
    private String source;
    private String remark;
    private Date createdAt;
}
