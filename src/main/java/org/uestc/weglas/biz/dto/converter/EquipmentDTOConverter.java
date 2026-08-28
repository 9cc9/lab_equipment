package org.uestc.weglas.biz.dto.converter;

import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
import org.uestc.weglas.biz.dto.EquipmentDTO;
import org.uestc.weglas.biz.dto.RoomDTO;
import org.uestc.weglas.core.model.Equipment;
import org.uestc.weglas.core.model.Room;

public final class EquipmentDTOConverter {

    private EquipmentDTOConverter() {
    }

    public static EquipmentDTO toDTO(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return EquipmentDTO.builder()
                .id(equipment.getId())
                .assetCode(equipment.getAssetCode())
                .name(equipment.getName())
                .brand(equipment.getBrand())
                .model(equipment.getModel())
                .serialNo(equipment.getSerialNo())
                .spec(equipment.getSpec())
                .quantity(equipment.getQuantity())
                .unit(equipment.getUnit())
                .unitPrice(equipment.getUnitPrice())
                .bookValue(equipment.getBookValue())
                .cardStatus(equipment.getCardStatus())
                .usageStatus(equipment.getUsageStatus())
                .roomId(equipment.getRoomId())
                .roomCode(equipment.getRoomCode())
                .roomName(equipment.getRoomName())
                .locationRaw(equipment.getLocationRaw())
                .locationNote(equipment.getLocationNote())
                .department(equipment.getDepartment())
                .building(equipment.getBuilding())
                .custodian(equipment.getCustodian())
                .purchaseDate(equipment.getPurchaseDate())
                .scrapDate(equipment.getScrapDate())
                .supplier(equipment.getSupplier())
                .manufacturer(equipment.getManufacturer())
                .isAbnormal(equipment.getIsAbnormal())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }

    public static RoomDTO toDTO(Room room) {
        if (room == null) {
            return null;
        }
        return RoomDTO.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .roomName(room.getRoomName())
                .building(room.getBuilding())
                .equipmentCount(room.getEquipmentCount())
                .build();
    }

    public static EquipmentChangeLogDTO toDTO(EquipmentChangeLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return EquipmentChangeLogDTO.builder()
                .id(entity.getId())
                .assetCode(entity.getAssetCode())
                .fieldName(entity.getFieldName())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .operatorName(entity.getOperatorName())
                .source(entity.getSource())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static Equipment toModel(EquipmentEntity entity, String roomCode, String roomName) {
        if (entity == null) {
            return null;
        }
        return Equipment.builder()
                .id(entity.getId())
                .assetCode(entity.getAssetCode())
                .name(entity.getName())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .serialNo(entity.getSerialNo())
                .spec(entity.getSpec())
                .quantity(entity.getQuantity())
                .unit(entity.getUnit())
                .unitPrice(entity.getUnitPrice())
                .bookValue(entity.getBookValue())
                .cardStatus(entity.getCardStatus())
                .usageStatus(entity.getUsageStatus())
                .roomId(entity.getRoomId())
                .roomCode(roomCode)
                .roomName(roomName)
                .locationRaw(entity.getLocationRaw())
                .locationNote(entity.getLocationNote())
                .department(entity.getDepartment())
                .building(entity.getBuilding())
                .custodian(entity.getCustodian())
                .purchaseDate(entity.getPurchaseDate())
                .scrapDate(entity.getScrapDate())
                .supplier(entity.getSupplier())
                .manufacturer(entity.getManufacturer())
                .isAbnormal(entity.getIsAbnormal())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
