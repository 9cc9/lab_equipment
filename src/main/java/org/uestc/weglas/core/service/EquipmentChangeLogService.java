package org.uestc.weglas.core.service;

import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;

import java.util.List;

public interface EquipmentChangeLogService {
    List<EquipmentChangeLogDTO> listByEquipmentId(String equipmentId, int limit);
}
