package org.uestc.weglas.core.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.base.dal.entity.RoomEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentChangeLogMapper;
import org.uestc.weglas.base.dal.mapper.EquipmentMapper;
import org.uestc.weglas.base.dal.mapper.RoomMapper;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.biz.dto.BatchDeleteEquipmentRequest;
import org.uestc.weglas.biz.dto.BatchUpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.BatchUpdateResultDTO;
import org.uestc.weglas.biz.dto.UpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;
import org.uestc.weglas.core.enums.Status;
import org.uestc.weglas.core.model.Equipment;
import org.uestc.weglas.core.model.Room;
import org.uestc.weglas.core.util.IdGenerator;
import org.uestc.weglas.core.util.UserContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private static final String NOT_FOUND_MESSAGE = "未找到该设备";

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

    @Autowired
    @Lazy
    private EquipmentService equipmentService;

    @Override
    public Equipment queryByAssetCode(String assetCode) {
        AssertUtil.notBlank(assetCode, "设备编号不能为空");
        EquipmentEntity entity = requireEnabledByAssetCode(assetCode.trim());
        return toEquipment(entity);
    }

    @Override
    public Equipment queryById(String id) {
        EquipmentEntity entity = equipmentMapper.selectById(id);
        AssertUtil.notNull(entity, NOT_FOUND_MESSAGE);
        assertEnabled(entity);
        return toEquipment(entity);
    }

    @Override
    public List<Equipment> listByRoomCode(String roomCode, String keyword, String usageStatus, String sortOrder) {
        AssertUtil.notBlank(roomCode, "房间编号不能为空");
        RoomEntity room = roomMapper.selectByRoomCode(roomCode.trim());
        if (room == null) {
            return new ArrayList<>();
        }
        String order = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";
        String statusFilter = StringUtils.isBlank(usageStatus) ? null : usageStatus.trim();
        List<EquipmentEntity> entities = equipmentMapper.selectByRoomId(room.getId(), keyword, statusFilter, order);
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
        EquipmentEntity entity = requireEnabledByAssetCode(assetCode.trim());

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
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus(Status.ENABLED.getCode());
        }
        entity.setUpdatedAt(now);
        equipmentMapper.updateById(entity);

        String source = StringUtils.isBlank(request.getSource()) ? "SCAN" : request.getSource();
        String remark = StringUtils.trimToNull(request.getRemark());
        saveChangeLog(entity, "usageStatus", oldUsageStatus, request.getUsageStatus(), operatorId, operatorName, source, remark, now);
        saveChangeLog(entity, "roomCode", oldRoomCode, room.getRoomCode(), operatorId, operatorName, source, remark, now);
        saveInspectionLog(entity, operatorId, operatorName, source, remark, now);

        return toEquipment(entity, toRoomEntity(room));
    }

    @Override
    public BatchUpdateResultDTO batchUpdateStatus(BatchUpdateEquipmentStatusRequest request,
                                                  String operatorId, String operatorName) {
        AssertUtil.notNull(request.getAssetCodes(), "请选择设备");

        List<String> codes = new ArrayList<>();
        for (String assetCode : request.getAssetCodes()) {
            if (StringUtils.isNotBlank(assetCode)) {
                codes.add(assetCode.trim());
            }
        }
        AssertUtil.isTrue(!codes.isEmpty(), "请选择设备");

        UpdateEquipmentStatusRequest single = new UpdateEquipmentStatusRequest();
        single.setUsageStatus(request.getUsageStatus());
        single.setRoomCode(request.getRoomCode());
        single.setRemark(request.getRemark());
        single.setSource(StringUtils.isBlank(request.getSource()) ? "BATCH" : request.getSource());

        int success = 0;
        List<String> errors = new ArrayList<>();
        for (String code : codes) {
            try {
                equipmentService.updateStatus(code, single, operatorId, operatorName);
                success++;
            } catch (Exception e) {
                errors.add(code + ": " + e.getMessage());
            }
        }
        int total = codes.size();
        return BatchUpdateResultDTO.builder()
                .totalCount(total)
                .successCount(success)
                .failCount(total - success)
                .errors(errors.size() > 20 ? errors.subList(0, 20) : errors)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Equipment delete(String assetCode, String remark, String source,
                            String operatorId, String operatorName) {
        assertAdmin();
        AssertUtil.notBlank(assetCode, "设备编号不能为空");

        EquipmentEntity entity = equipmentMapper.selectByAssetCode(assetCode.trim());
        AssertUtil.notNull(entity, NOT_FOUND_MESSAGE);
        AssertUtil.isTrue(!isDeleted(entity), "设备已删除");

        String oldStatus = entity.getStatus();
        Date now = new Date();
        entity.setStatus(Status.DELETED.getCode());
        entity.setUpdatedAt(now);
        equipmentMapper.updateById(entity);

        String logSource = StringUtils.isBlank(source) ? "DELETE" : source;
        saveChangeLog(entity, "status", oldStatus, Status.DELETED.getCode(),
                operatorId, operatorName, logSource, StringUtils.trimToNull(remark), now);
        return toEquipment(entity);
    }

    @Override
    public BatchUpdateResultDTO batchDelete(BatchDeleteEquipmentRequest request,
                                            String operatorId, String operatorName) {
        assertAdmin();
        AssertUtil.notNull(request.getAssetCodes(), "请选择设备");

        List<String> codes = new ArrayList<>();
        for (String assetCode : request.getAssetCodes()) {
            if (StringUtils.isNotBlank(assetCode)) {
                codes.add(assetCode.trim());
            }
        }
        AssertUtil.isTrue(!codes.isEmpty(), "请选择设备");

        String remark = request.getRemark();
        int success = 0;
        List<String> errors = new ArrayList<>();
        for (String code : codes) {
            try {
                equipmentService.delete(code, remark, "BATCH_DELETE", operatorId, operatorName);
                success++;
            } catch (Exception e) {
                errors.add(code + ": " + e.getMessage());
            }
        }
        int total = codes.size();
        return BatchUpdateResultDTO.builder()
                .totalCount(total)
                .successCount(success)
                .failCount(total - success)
                .errors(errors.size() > 20 ? errors.subList(0, 20) : errors)
                .build();
    }

    private EquipmentEntity requireEnabledByAssetCode(String assetCode) {
        EquipmentEntity entity = equipmentMapper.selectByAssetCode(assetCode);
        AssertUtil.notNull(entity, NOT_FOUND_MESSAGE);
        assertEnabled(entity);
        return entity;
    }

    private void assertEnabled(EquipmentEntity entity) {
        AssertUtil.isTrue(!isDeleted(entity), NOT_FOUND_MESSAGE);
    }

    private boolean isDeleted(EquipmentEntity entity) {
        Status status = Status.fromCode(entity.getStatus());
        return status != null && status.isDeleted();
    }

    private void assertAdmin() {
        AssertUtil.isTrue(UserContextHolder.isAdmin(), "仅管理员可删除设备");
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
                               String operatorId, String operatorName, String source, String remark, Date now) {
        if (StringUtils.equals(StringUtils.defaultString(oldValue), StringUtils.defaultString(newValue))) {
            return;
        }
        insertChangeLog(entity, fieldName, oldValue, newValue, operatorId, operatorName, source, remark, now);
    }

    private void saveInspectionLog(EquipmentEntity entity, String operatorId, String operatorName,
                                   String source, String remark, Date now) {
        insertChangeLog(entity, "清查", null, "完成设备清查", operatorId, operatorName, source, remark, now);
    }

    private void insertChangeLog(EquipmentEntity entity, String fieldName, String oldValue, String newValue,
                                   String operatorId, String operatorName, String source, String remark, Date now) {
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
        log.setRemark(remark);
        log.setCreatedAt(now);
        changeLogMapper.insert(log);
    }
}
