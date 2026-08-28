package org.uestc.weglas.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Room {
    private String id;
    private String roomCode;
    private String roomName;
    private String building;
    private Integer equipmentCount;
    private Integer lostEquipmentCount;
}
