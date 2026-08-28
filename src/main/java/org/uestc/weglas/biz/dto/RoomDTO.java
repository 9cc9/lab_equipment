package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class RoomDTO {
    private String id;
    private String roomCode;
    private String roomName;
    private String building;
    private Integer equipmentCount;
    private Integer lostEquipmentCount;
}
