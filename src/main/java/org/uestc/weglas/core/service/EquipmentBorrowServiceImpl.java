package org.uestc.weglas.core.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uestc.weglas.base.dal.entity.EquipmentBorrowEntity;
import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.base.dal.entity.RoomEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentBorrowMapper;
import org.uestc.weglas.base.dal.mapper.EquipmentChangeLogMapper;
import org.uestc.weglas.base.dal.mapper.EquipmentMapper;
import org.uestc.weglas.base.dal.mapper.RoomMapper;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.biz.dto.*;
import org.uestc.weglas.biz.dto.converter.BorrowDTOConverter;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;
import org.uestc.weglas.core.enums.BorrowStatus;
import org.uestc.weglas.core.enums.BorrowType;
import org.uestc.weglas.core.enums.Status;
import org.uestc.weglas.core.model.Equipment;
import org.uestc.weglas.core.model.UserContext;
import org.uestc.weglas.core.util.IdGenerator;
import org.uestc.weglas.core.util.UserContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EquipmentBorrowServiceImpl implements EquipmentBorrowService {

    private static final Set<String> BORROWABLE_USAGE = new HashSet<>(
            Arrays.asList("在用", "在用无条码"));

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private EquipmentBorrowMapper borrowMapper;

    @Autowired
    private EquipmentChangeLogMapper changeLogMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private AliyunOssService aliyunOssService;

    @Autowired
    @Lazy
    private EquipmentBorrowService equipmentBorrowService;

    @Override
    public List<BorrowEquipmentDTO> listBorrowableEquipment(String keyword, String roomCode, String borrowType) {
        assertLoggedIn();
        List<EquipmentEntity> entities = equipmentMapper.selectBorrowable();
        List<BorrowEquipmentDTO> result = new ArrayList<>();
        BorrowType filterType = StringUtils.isBlank(borrowType) ? null : BorrowType.fromCode(borrowType);
        String roomId = resolveRoomId(roomCode);

        for (EquipmentEntity entity : entities) {
            if (!isEquipmentBorrowableBase(entity)) {
                continue;
            }
            if (roomId != null && !roomId.equals(entity.getRoomId())) {
                continue;
            }
            if (StringUtils.isNotBlank(keyword) && !matchesKeyword(entity, keyword.trim())) {
                continue;
            }
            BorrowEquipmentDTO dto = toBorrowSummary(entity, true);
            if (!isAvailableForBorrowType(entity, dto, filterType)) {
                continue;
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public BorrowEquipmentDTO getBorrowEquipmentDetail(String assetCode, boolean studentView) {
        assertLoggedIn();
        EquipmentEntity entity = requireEnabledEquipment(assetCode);
        AssertUtil.isTrue(Boolean.TRUE.equals(entity.getBorrowable()), "该设备未开放借用");
        return toBorrowSummary(entity, studentView);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowApplicationDTO submitApplication(SubmitBorrowApplicationRequest request) {
        assertLoggedIn();
        BorrowType type = BorrowType.fromCode(request.getBorrowType());
        AssertUtil.notNull(type, "借用类型无效");

        EquipmentEntity equipment = requireEnabledEquipment(request.getAssetCode());
        AssertUtil.isTrue(Boolean.TRUE.equals(equipment.getBorrowable()), "该设备未开放借用");
        AssertUtil.isTrue(isAllowedBorrowType(equipment, type), "该设备不支持此借用类型");
        AssertUtil.isTrue(BORROWABLE_USAGE.contains(StringUtils.defaultString(equipment.getUsageStatus())),
                "设备当前现状不可借用");
        AssertUtil.isTrue(request.getExpectedEndAt().after(request.getExpectedStartAt()), "归还时间必须晚于开始时间");
        if (type == BorrowType.EXTERNAL) {
            AssertUtil.notBlank(request.getDestination(), "外借须填写外带目的地");
        }

        AssertUtil.isTrue(UserContextHolder.isStudent(), "仅同学本人可提交借用申请");

        String borrowerName = StringUtils.trimToNull(request.getBorrowerName());
        String studentNo = StringUtils.trimToNull(request.getStudentNo());
        String phone = StringUtils.trimToNull(request.getBorrowerPhone());
        UserContext ctx = UserContextHolder.get();
        if (borrowerName == null) {
            borrowerName = ctx.getName();
        }
        if (studentNo == null) {
            studentNo = ctx.getStudentNo();
        }
        if (phone == null) {
            phone = ctx.getPhone();
        }
        AssertUtil.notBlank(borrowerName, "请填写借用人姓名");
        AssertUtil.notBlank(studentNo, "请填写学号");
        AssertUtil.notBlank(phone, "请填写手机号");

        String identityProofUrl = StringUtils.trimToNull(request.getIdentityProofUrl());
        AssertUtil.notBlank(identityProofUrl, "请上传身份证明");
        AssertUtil.isTrue(aliyunOssService.isValidStoredUrl(identityProofUrl), "身份证明无效，请重新上传");

        if (type == BorrowType.EXTERNAL) {
            EquipmentBorrowEntity existing = borrowMapper.selectActiveExternalForUpdate(equipment.getId());
            AssertUtil.isTrue(existing == null, "该设备外借占用中");
        } else {
            EquipmentBorrowEntity existing = borrowMapper.selectActiveInLabByStudentForUpdate(equipment.getId(), studentNo);
            AssertUtil.isTrue(existing == null, "您在该设备上已有进行中的室内借用申请");
        }

        Date now = new Date();
        EquipmentBorrowEntity application = new EquipmentBorrowEntity();
        application.setId(idGenerator.generate(IdGenerator.EntityType.BORROW));
        application.setEquipmentId(equipment.getId());
        application.setAssetCode(equipment.getAssetCode());
        application.setEquipmentName(equipment.getName());
        application.setBorrowType(type.getCode());
        application.setStatus(BorrowStatus.PENDING.getCode());
        application.setExpectedStartAt(request.getExpectedStartAt());
        application.setExpectedEndAt(request.getExpectedEndAt());
        application.setBorrowerName(borrowerName);
        application.setStudentNo(studentNo);
        application.setBorrowerPhone(phone);
        application.setIdentityProofUrl(identityProofUrl);
        application.setPurpose(StringUtils.trimToNull(request.getPurpose()));
        application.setDestination(StringUtils.trimToNull(request.getDestination()));
        application.setRemark(StringUtils.trimToNull(request.getRemark()));
        application.setApplicantUserId(UserContextHolder.getUserId());
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        borrowMapper.insert(application);
        return BorrowDTOConverter.toDTO(application);
    }

    @Override
    public PageResultDTO<BorrowApplicationDTO> listMine(int page, int pageSize) {
        assertLoggedIn();
        String studentNo;
        if (UserContextHolder.isStudent()) {
            studentNo = UserContextHolder.get().getStudentNo();
        } else {
            AssertUtil.isTrue(false, "请使用管理端查看全部申请");
            return null;
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePage - 1) * safeSize;
        List<EquipmentBorrowEntity> entities = borrowMapper.selectByStudentNo(studentNo, offset, safeSize);
        long total = borrowMapper.countByStudentNo(studentNo);
        List<BorrowApplicationDTO> list = new ArrayList<>();
        for (EquipmentBorrowEntity entity : entities) {
            list.add(BorrowDTOConverter.toDTO(entity));
        }
        return PageResultDTO.<BorrowApplicationDTO>builder()
                .list(list)
                .total(total)
                .page(safePage)
                .pageSize(safeSize)
                .build();
    }

    @Override
    public BorrowApplicationDTO getApplication(String id) {
        assertLoggedIn();
        EquipmentBorrowEntity entity = borrowMapper.selectById(id);
        AssertUtil.notNull(entity, "申请不存在");
        if (UserContextHolder.isStudent()) {
            AssertUtil.isTrue(entity.getStudentNo().equals(UserContextHolder.get().getStudentNo()),
                    "无权查看该申请");
        }
        return BorrowDTOConverter.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowApplicationDTO cancelApplication(String id) {
        assertLoggedIn();
        EquipmentBorrowEntity entity = borrowMapper.selectById(id);
        AssertUtil.notNull(entity, "申请不存在");
        AssertUtil.isTrue(BorrowStatus.PENDING.getCode().equals(entity.getStatus()), "仅待审批申请可撤销");
        if (UserContextHolder.isStudent()) {
            AssertUtil.isTrue(entity.getStudentNo().equals(UserContextHolder.get().getStudentNo()),
                    "无权撤销该申请");
        } else {
            assertAdmin();
        }
        Date now = new Date();
        entity.setStatus(BorrowStatus.CANCELLED.getCode());
        entity.setUpdatedAt(now);
        borrowMapper.updateById(entity);
        return BorrowDTOConverter.toDTO(entity);
    }

    @Override
    public PageResultDTO<BorrowApplicationDTO> adminListApplications(int page, int pageSize,
                                                                   String status, String borrowType,
                                                                   String assetCode, String studentNo,
                                                                   Boolean overdueOnly) {
        assertAdmin();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePage - 1) * safeSize;
        List<EquipmentBorrowEntity> entities = borrowMapper.selectAdminPage(
                StringUtils.trimToNull(status),
                StringUtils.trimToNull(borrowType),
                StringUtils.trimToNull(assetCode),
                StringUtils.trimToNull(studentNo),
                overdueOnly,
                offset,
                safeSize);
        long total = borrowMapper.countAdminPage(
                StringUtils.trimToNull(status),
                StringUtils.trimToNull(borrowType),
                StringUtils.trimToNull(assetCode),
                StringUtils.trimToNull(studentNo),
                overdueOnly);
        List<BorrowApplicationDTO> list = new ArrayList<>();
        for (EquipmentBorrowEntity entity : entities) {
            list.add(BorrowDTOConverter.toDTO(entity));
        }
        return PageResultDTO.<BorrowApplicationDTO>builder()
                .list(list)
                .total(total)
                .page(safePage)
                .pageSize(safeSize)
                .build();
    }

    @Override
    public PageResultDTO<BorrowApplicationDTO> adminListDueWarnings(int page, int pageSize) {
        assertAdmin();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePage - 1) * safeSize;
        List<EquipmentBorrowEntity> entities = borrowMapper.selectDueWarningPage(offset, safeSize);
        long total = borrowMapper.countDueWarning();
        List<BorrowApplicationDTO> list = new ArrayList<>();
        for (EquipmentBorrowEntity entity : entities) {
            list.add(BorrowDTOConverter.toDTO(entity));
        }
        return PageResultDTO.<BorrowApplicationDTO>builder()
                .list(list)
                .total(total)
                .page(safePage)
                .pageSize(safeSize)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowApplicationDTO approve(String id, String remark) {
        assertAdmin();
        EquipmentBorrowEntity entity = borrowMapper.selectById(id);
        AssertUtil.notNull(entity, "申请不存在");
        AssertUtil.isTrue(BorrowStatus.PENDING.getCode().equals(entity.getStatus()), "申请状态不是待审批");

        EquipmentEntity equipment = equipmentMapper.selectById(entity.getEquipmentId());
        AssertUtil.notNull(equipment, "设备不存在");
        AssertUtil.isTrue(Boolean.TRUE.equals(equipment.getBorrowable()), "设备已关闭借用");
        AssertUtil.isTrue(BORROWABLE_USAGE.contains(StringUtils.defaultString(equipment.getUsageStatus()))
                        || (BorrowType.EXTERNAL.getCode().equals(entity.getBorrowType())
                        && "借出".equals(equipment.getUsageStatus())),
                "设备当前不可借");

        BorrowType type = BorrowType.fromCode(entity.getBorrowType());
        if (type == BorrowType.EXTERNAL) {
            EquipmentBorrowEntity conflict = borrowMapper.selectActiveExternalForUpdate(equipment.getId());
            AssertUtil.isTrue(conflict == null || conflict.getId().equals(entity.getId()), "该设备外借占用中");
        }

        Date now = new Date();
        UserContext ctx = UserContextHolder.get();
        entity.setStatus(BorrowStatus.APPROVED.getCode());
        entity.setActualStartAt(now);
        entity.setApproverId(ctx != null ? ctx.getUserId() : null);
        entity.setApproverName(ctx != null ? ctx.getName() : null);
        entity.setApproveRemark(StringUtils.trimToNull(remark));
        entity.setApprovedAt(now);
        entity.setUpdatedAt(now);

        if (type == BorrowType.EXTERNAL) {
            entity.setPrevUsageStatus(equipment.getUsageStatus());
            equipment.setUsageStatus("借出");
            equipment.setUpdatedAt(now);
            equipmentMapper.updateById(equipment);
            insertChangeLog(equipment, "usageStatus", entity.getPrevUsageStatus(), "借出",
                    ctx, "BORROW", buildBorrowRemark(entity), now);
        }
        borrowMapper.updateById(entity);
        return BorrowDTOConverter.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowApplicationDTO reject(String id, String remark) {
        assertAdmin();
        AssertUtil.notBlank(remark, "请填写驳回原因");
        EquipmentBorrowEntity entity = borrowMapper.selectById(id);
        AssertUtil.notNull(entity, "申请不存在");
        AssertUtil.isTrue(BorrowStatus.PENDING.getCode().equals(entity.getStatus()), "申请状态不是待审批");
        Date now = new Date();
        UserContext ctx = UserContextHolder.get();
        entity.setStatus(BorrowStatus.REJECTED.getCode());
        entity.setApproverId(ctx != null ? ctx.getUserId() : null);
        entity.setApproverName(ctx != null ? ctx.getName() : null);
        entity.setApproveRemark(remark.trim());
        entity.setApprovedAt(now);
        entity.setUpdatedAt(now);
        borrowMapper.updateById(entity);
        return BorrowDTOConverter.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowApplicationDTO requestReturn(String id, String remark) {
        assertLoggedIn();
        AssertUtil.isTrue(UserContextHolder.isStudent(), "仅同学可提交借用结束申请");
        EquipmentBorrowEntity entity = borrowMapper.selectById(id);
        AssertUtil.notNull(entity, "申请不存在");
        AssertUtil.isTrue(BorrowStatus.APPROVED.getCode().equals(entity.getStatus()), "当前状态不可申请借用结束");
        UserContext ctx = UserContextHolder.get();
        AssertUtil.isTrue(ctx != null && entity.getStudentNo().equals(ctx.getStudentNo()), "无权操作该申请");
        Date now = new Date();
        entity.setStatus(BorrowStatus.RETURN_PENDING.getCode());
        entity.setReturnRemark(StringUtils.trimToNull(remark));
        entity.setUpdatedAt(now);
        borrowMapper.updateById(entity);
        return BorrowDTOConverter.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowApplicationDTO confirmReturn(String id, String remark) {
        assertStaff();
        EquipmentBorrowEntity entity = borrowMapper.selectById(id);
        AssertUtil.notNull(entity, "申请不存在");
        String st = entity.getStatus();
        AssertUtil.isTrue(BorrowStatus.APPROVED.getCode().equals(st)
                        || BorrowStatus.RETURN_PENDING.getCode().equals(st),
                "申请不在借用中");

        Date now = new Date();
        UserContext ctx = UserContextHolder.get();
        entity.setStatus(BorrowStatus.RETURNED.getCode());
        entity.setActualEndAt(now);
        entity.setReturnerId(ctx != null ? ctx.getUserId() : null);
        entity.setReturnerName(ctx != null ? ctx.getName() : null);
        entity.setReturnRemark(StringUtils.trimToNull(remark));
        entity.setUpdatedAt(now);

        if (BorrowType.EXTERNAL.getCode().equals(entity.getBorrowType())) {
            EquipmentEntity equipment = equipmentMapper.selectById(entity.getEquipmentId());
            if (equipment != null && "借出".equals(equipment.getUsageStatus())) {
                List<EquipmentBorrowEntity> approved = borrowMapper.selectApprovedByEquipmentId(equipment.getId());
                boolean otherExternal = false;
                for (EquipmentBorrowEntity other : approved) {
                    if (!other.getId().equals(entity.getId())
                            && BorrowType.EXTERNAL.getCode().equals(other.getBorrowType())) {
                        otherExternal = true;
                        break;
                    }
                }
                if (!otherExternal) {
                    String restore = StringUtils.isNotBlank(entity.getPrevUsageStatus())
                            ? entity.getPrevUsageStatus() : "在用";
                    String old = equipment.getUsageStatus();
                    equipment.setUsageStatus(restore);
                    equipment.setUpdatedAt(now);
                    equipmentMapper.updateById(equipment);
                    insertChangeLog(equipment, "usageStatus", old, restore,
                            ctx, "RETURN", buildBorrowRemark(entity), now);
                }
            }
        }
        borrowMapper.updateById(entity);
        return BorrowDTOConverter.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentDTO updateEquipmentBorrow(String assetCode, UpdateEquipmentBorrowRequest request) {
        assertAdmin();
        EquipmentEntity entity = requireEnabledEquipment(assetCode);
        if (request.getBorrowable() != null) {
            entity.setBorrowable(request.getBorrowable());
        }
        if (StringUtils.isNotBlank(request.getAllowedBorrowTypes())) {
            entity.setAllowedBorrowTypes(request.getAllowedBorrowTypes().trim());
        }
        entity.setUpdatedAt(new Date());
        equipmentMapper.updateBorrowFlags(entity);
        return buildStaffEquipmentDto(entity);
    }

    @Override
    public BatchUpdateResultDTO batchUpdateEquipmentBorrow(BatchUpdateEquipmentBorrowRequest request) {
        assertAdmin();
        AssertUtil.notNull(request.getAssetCodes(), "请选择设备");
        int success = 0;
        List<String> errors = new ArrayList<>();
        UpdateEquipmentBorrowRequest single = new UpdateEquipmentBorrowRequest();
        single.setBorrowable(request.getBorrowable());
        single.setAllowedBorrowTypes(request.getAllowedBorrowTypes());
        for (String code : request.getAssetCodes()) {
            if (StringUtils.isBlank(code)) {
                continue;
            }
            try {
                equipmentBorrowService.updateEquipmentBorrow(code.trim(), single);
                success++;
            } catch (Exception e) {
                errors.add(code + ": " + e.getMessage());
            }
        }
        int total = request.getAssetCodes().size();
        return BatchUpdateResultDTO.builder()
                .totalCount(total)
                .successCount(success)
                .failCount(total - success)
                .errors(errors.size() > 20 ? errors.subList(0, 20) : errors)
                .build();
    }

    @Override
    public List<ActiveBorrowDTO> listActiveBorrowsForEquipment(String equipmentId, boolean includePhone) {
        return BorrowDTOConverter.toActiveList(borrowMapper.selectApprovedByEquipmentId(equipmentId), includePhone);
    }

    @Override
    public void assertNoActiveBorrowConflict(String equipmentId, String newUsageStatus) {
        if (borrowMapper.countActiveByEquipmentId(equipmentId) > 0) {
            if (!BORROWABLE_USAGE.contains(StringUtils.defaultString(newUsageStatus))) {
                AssertUtil.isTrue(false, "设备有进行中的借用申请，请先处理借用后再修改现状");
            }
        }
        List<EquipmentBorrowEntity> approved = borrowMapper.selectApprovedByEquipmentId(equipmentId);
        for (EquipmentBorrowEntity entity : approved) {
            if (BorrowType.EXTERNAL.getCode().equals(entity.getBorrowType())
                    && !"借出".equals(newUsageStatus)) {
                AssertUtil.isTrue(false, "设备外借中，请先确认归还后再修改现状");
            }
        }
    }

    private BorrowEquipmentDTO toBorrowSummary(EquipmentEntity entity, boolean studentView) {
        RoomEntity room = entity.getRoomId() != null ? roomMapper.selectById(entity.getRoomId()) : null;
        String roomCode = room != null ? room.getRoomCode() : null;
        String roomName = room != null ? room.getRoomName() : null;

        boolean externalBlocked = borrowMapper.selectActiveExternal(entity.getId()) != null;
        int inLabCount = borrowMapper.countApprovedInLabByEquipmentId(entity.getId());
        List<ActiveBorrowDTO> activeBorrows = studentView
                ? new ArrayList<>()
                : BorrowDTOConverter.toActiveList(borrowMapper.selectApprovedByEquipmentId(entity.getId()), true);

        return BorrowEquipmentDTO.builder()
                .id(entity.getId())
                .assetCode(entity.getAssetCode())
                .name(entity.getName())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .roomCode(roomCode)
                .roomName(roomName)
                .usageStatus(entity.getUsageStatus())
                .allowedBorrowTypes(defaultAllowedTypes(entity))
                .externalBorrowBlocked(externalBlocked)
                .inLabActiveCount(inLabCount)
                .activeBorrows(activeBorrows)
                .build();
    }

    public EquipmentDTO buildStaffEquipmentDto(EquipmentEntity entity) {
        RoomEntity room = entity.getRoomId() != null ? roomMapper.selectById(entity.getRoomId()) : null;
        String roomCode = room != null ? room.getRoomCode() : null;
        String roomName = room != null ? room.getRoomName() : null;
        Equipment equipment = EquipmentDTOConverter.toModel(entity, roomCode, roomName);
        EquipmentDTO dto = EquipmentDTOConverter.toDTO(equipment);
        dto.setBorrowable(entity.getBorrowable());
        dto.setAllowedBorrowTypes(defaultAllowedTypes(entity));
        dto.setActiveBorrows(listActiveBorrowsForEquipment(entity.getId(), true));
        dto.setInLabActiveCount(borrowMapper.countApprovedInLabByEquipmentId(entity.getId()));
        dto.setExternalBorrowBlocked(borrowMapper.selectActiveExternal(entity.getId()) != null);
        return dto;
    }

    private boolean isAvailableForBorrowType(EquipmentEntity entity, BorrowEquipmentDTO dto, BorrowType filterType) {
        if (filterType == null) {
            return true;
        }
        if (filterType == BorrowType.EXTERNAL) {
            return isAllowedBorrowType(entity, BorrowType.EXTERNAL)
                    && !dto.getExternalBorrowBlocked()
                    && BORROWABLE_USAGE.contains(StringUtils.defaultString(entity.getUsageStatus()));
        }
        return isAllowedBorrowType(entity, BorrowType.IN_LAB)
                && BORROWABLE_USAGE.contains(StringUtils.defaultString(entity.getUsageStatus()));
    }

    private boolean isEquipmentBorrowableBase(EquipmentEntity entity) {
        return entity != null
                && Status.ENABLED.getCode().equals(entity.getStatus())
                && Boolean.TRUE.equals(entity.getBorrowable());
    }

    private boolean isAllowedBorrowType(EquipmentEntity entity, BorrowType type) {
        String allowed = defaultAllowedTypes(entity);
        if ("ALL".equalsIgnoreCase(allowed)) {
            return true;
        }
        return type.getCode().equalsIgnoreCase(allowed);
    }

    private String defaultAllowedTypes(EquipmentEntity entity) {
        return StringUtils.isBlank(entity.getAllowedBorrowTypes()) ? "ALL" : entity.getAllowedBorrowTypes();
    }

    private boolean matchesKeyword(EquipmentEntity entity, String keyword) {
        return StringUtils.containsIgnoreCase(entity.getAssetCode(), keyword)
                || StringUtils.containsIgnoreCase(entity.getName(), keyword)
                || StringUtils.containsIgnoreCase(entity.getBrand(), keyword);
    }

    private String resolveRoomId(String roomCode) {
        if (StringUtils.isBlank(roomCode)) {
            return null;
        }
        RoomEntity room = roomMapper.selectByRoomCode(roomCode.trim());
        return room != null ? room.getId() : "__none__";
    }

    private EquipmentEntity requireEnabledEquipment(String assetCode) {
        AssertUtil.notBlank(assetCode, "设备编号不能为空");
        EquipmentEntity entity = equipmentMapper.selectByAssetCode(assetCode.trim());
        AssertUtil.notNull(entity, "未找到该设备");
        AssertUtil.isTrue(Status.ENABLED.getCode().equals(entity.getStatus()), "未找到该设备");
        return entity;
    }

    private void insertChangeLog(EquipmentEntity equipment, String field, String oldVal, String newVal,
                                 UserContext ctx, String source, String remark, Date now) {
        EquipmentChangeLogEntity log = new EquipmentChangeLogEntity();
        log.setId(idGenerator.generate(IdGenerator.EntityType.CHANGE_LOG));
        log.setEquipmentId(equipment.getId());
        log.setAssetCode(equipment.getAssetCode());
        log.setFieldName(field);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setOperatorId(ctx != null ? ctx.getUserId() : null);
        log.setOperatorName(ctx != null ? ctx.getName() : null);
        log.setSource(source);
        log.setRemark(remark);
        log.setCreatedAt(now);
        changeLogMapper.insert(log);
    }

    private String buildBorrowRemark(EquipmentBorrowEntity entity) {
        return entity.getStudentNo() + " " + entity.getBorrowerName() + " " + entity.getBorrowType();
    }

    private void assertLoggedIn() {
        AssertUtil.notNull(UserContextHolder.get(), "请先登录");
    }

    private void assertAdmin() {
        AssertUtil.isTrue(UserContextHolder.isAdmin(), "仅管理员可执行该操作");
    }

    private void assertStaff() {
        AssertUtil.isTrue(UserContextHolder.isStaff(), "无权执行该操作");
    }
}
