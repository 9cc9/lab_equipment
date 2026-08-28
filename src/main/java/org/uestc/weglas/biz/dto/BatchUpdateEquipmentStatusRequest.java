package org.uestc.weglas.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchUpdateEquipmentStatusRequest {
    @NotEmpty
    private List<String> assetCodes;
    @NotBlank
    private String usageStatus;
    @NotBlank
    private String roomCode;
    private String remark;
    private String source;
}
