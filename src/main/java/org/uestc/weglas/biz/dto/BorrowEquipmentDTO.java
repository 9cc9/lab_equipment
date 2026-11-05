package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BorrowEquipmentDTO {
    private String id;
    private String assetCode;
    private String name;
    private String brand;
    private String model;
    private String roomCode;
    private String roomName;
    private String usageStatus;
    private String allowedBorrowTypes;
    private Boolean externalBorrowBlocked;
    private Integer inLabActiveCount;
    private List<ActiveBorrowDTO> activeBorrows;
}
