package org.uestc.weglas.base.dal.entity;

import lombok.Data;

import java.util.Date;

@Data
public class EquipmentBorrowEntity {
    private String id;
    private String equipmentId;
    private String assetCode;
    private String equipmentName;
    private String borrowType;
    private String status;
    private Date expectedStartAt;
    private Date expectedEndAt;
    private Date actualStartAt;
    private Date actualEndAt;
    private String borrowerName;
    private String studentNo;
    private String borrowerPhone;
    private String purpose;
    private String destination;
    private String remark;
    private String applicantUserId;
    private String approverId;
    private String approverName;
    private String approveRemark;
    private Date approvedAt;
    private String returnerId;
    private String returnerName;
    private String returnRemark;
    private String prevUsageStatus;
    private Date createdAt;
    private Date updatedAt;
}
