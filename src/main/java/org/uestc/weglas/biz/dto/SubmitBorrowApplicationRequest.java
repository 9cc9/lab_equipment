package org.uestc.weglas.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class SubmitBorrowApplicationRequest {
    @NotBlank(message = "请选择设备")
    private String assetCode;
    @NotBlank(message = "请选择借用类型")
    private String borrowType;
    @NotNull(message = "请选择开始时间")
    private Date expectedStartAt;
    @NotNull(message = "请选择归还时间")
    private Date expectedEndAt;
    private String borrowerName;
    private String studentNo;
    private String borrowerPhone;
    @NotBlank(message = "请上传身份证明")
    private String identityProofUrl;
    @NotBlank(message = "请填写借用用途")
    @Size(min = 4, max = 256, message = "借用用途至少 4 个字，请写清楚原因")
    private String purpose;
    private String destination;
    private String remark;
}
