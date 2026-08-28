package org.uestc.weglas.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateEquipmentStatusRequest {
    @NotBlank
    private String usageStatus;
    @NotBlank
    private String roomCode;
    private String remark;
    private String source;
}
