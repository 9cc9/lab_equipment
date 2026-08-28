package org.uestc.weglas.base.dal.entity;

import lombok.Data;

import java.util.Date;

@Data
public class EquipmentChangeLogEntity {
    private String id;
    private String equipmentId;
    private String assetCode;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String operatorId;
    private String operatorName;
    private String source;
    private String remark;
    private Date createdAt;
}
