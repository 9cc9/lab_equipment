package org.uestc.weglas.base.dal.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class EquipmentEntity {
    private String id;
    private String assetCode;
    private String name;
    private String brand;
    private String model;
    private String serialNo;
    private String spec;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal bookValue;
    private String cardStatus;
    private String usageStatus;
    private String status;
    private String roomId;
    private String locationRaw;
    private String locationNote;
    private String department;
    private String building;
    private String custodian;
    private Date purchaseDate;
    private Date scrapDate;
    private String supplier;
    private String manufacturer;
    private Boolean isAbnormal;
    private String extInfo;
    private Date createdAt;
    private Date updatedAt;
}
