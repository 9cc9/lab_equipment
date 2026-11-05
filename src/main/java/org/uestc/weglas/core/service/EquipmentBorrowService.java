package org.uestc.weglas.core.service;

import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.biz.dto.*;

import java.util.List;

public interface EquipmentBorrowService {

    List<BorrowEquipmentDTO> listBorrowableEquipment(String keyword, String roomCode, String borrowType);

    BorrowEquipmentDTO getBorrowEquipmentDetail(String assetCode, boolean studentView);

    BorrowApplicationDTO submitApplication(SubmitBorrowApplicationRequest request);

    PageResultDTO<BorrowApplicationDTO> listMine(int page, int pageSize);

    BorrowApplicationDTO getApplication(String id);

    BorrowApplicationDTO cancelApplication(String id);

    PageResultDTO<BorrowApplicationDTO> adminListApplications(int page, int pageSize,
                                                              String status, String borrowType,
                                                              String assetCode, String studentNo,
                                                              Boolean overdueOnly);

    PageResultDTO<BorrowApplicationDTO> adminListDueWarnings(int page, int pageSize);

    BorrowApplicationDTO approve(String id, String remark);

    BorrowApplicationDTO reject(String id, String remark);

    BorrowApplicationDTO requestReturn(String id, String remark);

    BorrowApplicationDTO confirmReturn(String id, String remark);

    EquipmentDTO updateEquipmentBorrow(String assetCode, UpdateEquipmentBorrowRequest request);

    BatchUpdateResultDTO batchUpdateEquipmentBorrow(BatchUpdateEquipmentBorrowRequest request);

    EquipmentDTO buildStaffEquipmentDto(EquipmentEntity entity);

    List<ActiveBorrowDTO> listActiveBorrowsForEquipment(String equipmentId, boolean includePhone);

    void assertNoActiveBorrowConflict(String equipmentId, String newUsageStatus);
}
