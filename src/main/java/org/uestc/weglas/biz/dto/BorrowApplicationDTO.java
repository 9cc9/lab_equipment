package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class BorrowApplicationDTO {
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
    private String approverName;
    private String approveRemark;
    private Date approvedAt;
    private String returnerName;
    private String returnRemark;
    private Boolean overdue;
    /** OVERDUE=已超时, DUE_SOON=7天内到期 */
    private String dueWarningLevel;
    private Date createdAt;
    private Date updatedAt;
}
