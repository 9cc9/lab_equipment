package org.uestc.weglas.core.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.base.dal.entity.RoomEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentChangeLogMapper;
import org.uestc.weglas.base.dal.mapper.EquipmentMapper;
import org.uestc.weglas.base.dal.mapper.RoomMapper;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.biz.dto.UpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;
import org.uestc.weglas.core.model.Equipment;
import org.uestc.weglas.core.model.Room;
import org.uestc.weglas.core.util.IdGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private EquipmentChangeLogMapper changeLogMapper;

    @Autowired
    private RoomService roomService;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public Equipment queryByAssetCode(String assetCode) {
        AssertUtil.notBlank(assetCode, "设备编号不能为空");
        EquipmentEntity entity = equipmentMapper.selectByAssetCode(assetCode.trim());
        AssertUtil.notNull(entity, "未找到该设备");
        return toEquipment(entity);
    }

    @Override
    public Equipment queryById(String id) {
        EquipmentEntity entity = equipmentMapper.selectById(id);
        AssertUtil.notNull(entity, "未找到该设备");
        return toEquipment(entity);
    }

    @Override
    public List<Equipment> listByRoomCode(String roomCode, String keyword) {
        AssertUtil.notBlank(roomCode, "房间编号不能为空");
        RoomEntity room = roomMapper.selectByRoomCode(roomCode.trim());
        if (room == null) {
            return new ArrayList<>();
        }
        List<EquipmentEntity> entities = equipmentMapper.selectByRoomId(room.getId(), keyword);
        List<Equipment> result = new ArrayList<>();
        for (EquipmentEntity entity : entities) {
            result.add(toEquipment(entity, room));
        }
        return result;
    }

    @Override
    public List<Equipment> search(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        List<EquipmentEntity> entities = equipmentMapper.search(keyword.trim());
        List<Equipment> result = new ArrayList<>();
        for (EquipmentEntity entity : entities) {
            result.add(toEquipment(entity));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Equipment updateStatus(String assetCode, UpdateEquipmentStatusRequest request,
                                  String operatorId, String operatorName) {
        EquipmentEntity entity = equipmentMapper.selectByAssetCode(assetCode.trim());
        AssertUtil.notNull(entity, "未找到该设备");

        Room room = roomService.getOrCreateByRoomCode(request.getRoomCode(), entity.getBuilding());
        AssertUtil.notNull(room, "房间无效");

        String oldUsageStatus = entity.getUsageStatus();
        String oldRoomId = entity.getRoomId();
        String oldRoomCode = resolveRoomCode(oldRoomId);

        Date now = new Date();
        entity.setUsageStatus(request.getUsageStatus());
        entity.setRoomId(room.getId());
        entity.setLocationRaw(room.getRoomCode());
        if (StringUtils.isNotBlank(request.getRemark())) {
            entity.setLocationNote(request.getRemark());
        }
        entity.setUpdatedAt(now);
        equipmentMapper.updateById(entity);

        String source = StringUtils.isBlank(request.getSource()) ? "SCAN" : request.getSource();
        saveChangeLog(entity, "usageStatus", oldUsageStatus, request.getUsageStatus(), operatorId, operatorName, source, now);
        saveChangeLog(entity, "roomCode", oldRoomCode, room.getRoomCode(), operatorId, operatorName, source, now);

        return toEquipment(entity, toRoomEntity(room));
    }

    private Equipment toEquipment(EquipmentEntity entity) {
        RoomEntity room = entity.getRoomId() != null ? roomMapper.selectById(entity.getRoomId()) : null;
        return toEquipment(entity, room);
    }

    private Equipment toEquipment(EquipmentEntity entity, RoomEntity room) {
        String roomCode = room != null ? room.getRoomCode() : null;
        String roomName = room != null ? room.getRoomName() : null;
        return EquipmentDTOConverter.toModel(entity, roomCode, roomName);
    }

    private RoomEntity toRoomEntity(Room room) {
        RoomEntity entity = new RoomEntity();
        entity.setId(room.getId());
        entity.setRoomCode(room.getRoomCode());
        entity.setRoomName(room.getRoomName());
        entity.setBuilding(room.getBuilding());
        return entity;
    }

    private String resolveRoomCode(String roomId) {
        if (StringUtils.isBlank(roomId)) {
            return null;
        }
        RoomEntity room = roomMapper.selectById(roomId);
        return room != null ? room.getRoomCode() : null;
    }

    private void saveChangeLog(EquipmentEntity entity, String fieldName, String oldValue, String newValue,
                               String operatorId, String operatorName, String source, Date now) {
        if (StringUtils.equals(StringUtils.defaultString(oldValue), StringUtils.defaultString(newValue))) {
            return;
        }
        EquipmentChangeLogEntity log = new EquipmentChangeLogEntity();
        log.setId(idGenerator.generate(IdGenerator.EntityType.CHANGE_LOG));
        log.setEquipmentId(entity.getId());
        log.setAssetCode(entity.getAssetCode());
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setSource(source);
        log.setCreatedAt(now);
        changeLogMapper.insert(log);
    }
}
