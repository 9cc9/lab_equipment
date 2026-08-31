package org.uestc.weglas.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentChangeLogMapper;
import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
import org.uestc.weglas.biz.dto.PageResultDTO;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipmentChangeLogServiceImpl implements EquipmentChangeLogService {

    @Autowired
    private EquipmentChangeLogMapper changeLogMapper;

    @Override
    public List<EquipmentChangeLogDTO> listByEquipmentId(String equipmentId, int limit) {
        List<EquipmentChangeLogEntity> entities = changeLogMapper.selectByEquipmentId(equipmentId, limit);
        List<EquipmentChangeLogDTO> result = new ArrayList<>();
        for (EquipmentChangeLogEntity entity : entities) {
            result.add(EquipmentDTOConverter.toDTO(entity));
        }
        return result;
    }

    @Override
    public PageResultDTO<EquipmentChangeLogDTO> pageRoomAndStatusChanges(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePage - 1) * safePageSize;

        long total = changeLogMapper.countRoomAndStatusChanges();
        List<EquipmentChangeLogEntity> entities = changeLogMapper.selectRoomAndStatusChanges(offset, safePageSize);
        List<EquipmentChangeLogDTO> list = new ArrayList<>();
        for (EquipmentChangeLogEntity entity : entities) {
            list.add(EquipmentDTOConverter.toDTO(entity));
        }
        return PageResultDTO.<EquipmentChangeLogDTO>builder()
                .list(list)
                .total(total)
                .page(safePage)
                .pageSize(safePageSize)
                .build();
    }
}
