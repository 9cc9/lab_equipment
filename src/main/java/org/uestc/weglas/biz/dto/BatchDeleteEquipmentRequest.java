package org.uestc.weglas.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchDeleteEquipmentRequest {
    @NotEmpty
    private List<String> assetCodes;
    private String remark;
}
