package org.uestc.weglas.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentChangeLogMapper;
import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
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
}
