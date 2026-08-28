package org.uestc.weglas.core.service;

import org.uestc.weglas.biz.dto.UpdateEquipmentStatusRequest;
import org.uestc.weglas.core.model.Equipment;

import java.util.List;

public interface EquipmentService {

    Equipment queryByAssetCode(String assetCode);

    Equipment queryById(String id);

    List<Equipment> listByRoomCode(String roomCode, String keyword);

    List<Equipment> search(String keyword);

    Equipment updateStatus(String assetCode, UpdateEquipmentStatusRequest request,
                           String operatorId, String operatorName);
}
