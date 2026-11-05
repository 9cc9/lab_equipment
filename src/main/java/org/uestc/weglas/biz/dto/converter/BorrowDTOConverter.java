package org.uestc.weglas.biz.dto.converter;

import org.uestc.weglas.base.dal.entity.EquipmentBorrowEntity;
import org.uestc.weglas.biz.dto.ActiveBorrowDTO;
import org.uestc.weglas.biz.dto.BorrowApplicationDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class BorrowDTOConverter {

    private BorrowDTOConverter() {
    }

    public static BorrowApplicationDTO toDTO(EquipmentBorrowEntity entity) {
        if (entity == null) {
            return null;
        }
        return BorrowApplicationDTO.builder()
                .id(entity.getId())
                .equipmentId(entity.getEquipmentId())
                .assetCode(entity.getAssetCode())
                .equipmentName(entity.getEquipmentName())
                .borrowType(entity.getBorrowType())
                .status(entity.getStatus())
                .expectedStartAt(entity.getExpectedStartAt())
                .expectedEndAt(entity.getExpectedEndAt())
                .actualStartAt(entity.getActualStartAt())
                .actualEndAt(entity.getActualEndAt())
                .borrowerName(entity.getBorrowerName())
                .studentNo(entity.getStudentNo())
                .borrowerPhone(entity.getBorrowerPhone())
                .purpose(entity.getPurpose())
                .destination(entity.getDestination())
                .remark(entity.getRemark())
                .approverName(entity.getApproverName())
                .approveRemark(entity.getApproveRemark())
                .approvedAt(entity.getApprovedAt())
                .returnerName(entity.getReturnerName())
                .returnRemark(entity.getReturnRemark())
                .overdue(isOverdue(entity))
                .dueWarningLevel(dueWarningLevel(entity))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ActiveBorrowDTO toActiveDTO(EquipmentBorrowEntity entity, boolean includePhone) {
        if (entity == null) {
            return null;
        }
        ActiveBorrowDTO.ActiveBorrowDTOBuilder builder = ActiveBorrowDTO.builder()
                .id(entity.getId())
                .borrowType(entity.getBorrowType())
                .status(entity.getStatus())
                .borrowerName(entity.getBorrowerName())
                .studentNo(entity.getStudentNo())
                .expectedStartAt(entity.getExpectedStartAt())
                .expectedEndAt(entity.getExpectedEndAt())
                .overdue(isOverdue(entity));
        if (includePhone) {
            builder.borrowerPhone(entity.getBorrowerPhone());
        }
        return builder.build();
    }

    public static List<ActiveBorrowDTO> toActiveList(List<EquipmentBorrowEntity> entities, boolean includePhone) {
        List<ActiveBorrowDTO> list = new ArrayList<>();
        if (entities == null) {
            return list;
        }
        for (EquipmentBorrowEntity entity : entities) {
            list.add(toActiveDTO(entity, includePhone));
        }
        return list;
    }

    public static boolean isOverdue(EquipmentBorrowEntity entity) {
        return "OVERDUE".equals(dueWarningLevel(entity));
    }

    /**
     * 进行中借用：已超时或 7 天内到期返回级别，否则 null。
     */
    public static String dueWarningLevel(EquipmentBorrowEntity entity) {
        if (entity == null) {
            return null;
        }
        String st = entity.getStatus();
        if (!"APPROVED".equals(st) && !"RETURN_PENDING".equals(st)) {
            return null;
        }
        Date end = entity.getExpectedEndAt();
        if (end == null) {
            return null;
        }
        Date now = new Date();
        if (!end.after(now)) {
            return "OVERDUE";
        }
        long millisLeft = end.getTime() - now.getTime();
        long sevenDays = 7L * 24 * 60 * 60 * 1000;
        if (millisLeft <= sevenDays) {
            return "DUE_SOON";
        }
        return null;
    }
}
