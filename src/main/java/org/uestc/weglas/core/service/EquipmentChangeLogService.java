package org.uestc.weglas.core.service;

import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
import org.uestc.weglas.biz.dto.PageResultDTO;

import java.util.List;

public interface EquipmentChangeLogService {
    List<EquipmentChangeLogDTO> listByEquipmentId(String equipmentId, int limit);

    PageResultDTO<EquipmentChangeLogDTO> pageRoomAndStatusChanges(int page, int pageSize);
}
