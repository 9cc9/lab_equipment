package org.uestc.weglas.core.service;

import org.uestc.weglas.biz.dto.BatchDeleteEquipmentRequest;
import org.uestc.weglas.biz.dto.BatchUpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.BatchUpdateResultDTO;
import org.uestc.weglas.biz.dto.UpdateEquipmentStatusRequest;
import org.uestc.weglas.core.model.Equipment;

import java.util.List;

public interface EquipmentService {

    Equipment queryByAssetCode(String assetCode);

    Equipment queryById(String id);

    List<Equipment> listByRoomCode(String roomCode, String keyword, String usageStatus, String sortOrder);

    List<Equipment> search(String keyword);

    Equipment updateStatus(String assetCode, UpdateEquipmentStatusRequest request,
                           String operatorId, String operatorName);

    BatchUpdateResultDTO batchUpdateStatus(BatchUpdateEquipmentStatusRequest request,
                                           String operatorId, String operatorName);

    Equipment delete(String assetCode, String remark, String source,
                     String operatorId, String operatorName);

    BatchUpdateResultDTO batchDelete(BatchDeleteEquipmentRequest request,
                                     String operatorId, String operatorName);
}
