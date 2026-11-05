package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ActiveBorrowDTO {
    private String id;
    private String borrowType;
    private String status;
    private String borrowerName;
    private String studentNo;
    private String borrowerPhone;
    private Date expectedStartAt;
    private Date expectedEndAt;
    private Boolean overdue;
}
