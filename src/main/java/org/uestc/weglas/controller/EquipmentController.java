package org.uestc.weglas.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.validator.RequestValidator;
import org.uestc.weglas.biz.dto.BatchDeleteEquipmentRequest;
import org.uestc.weglas.biz.dto.BatchUpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.BatchUpdateResultDTO;
import org.uestc.weglas.biz.dto.DeleteEquipmentRequest;
import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
import org.uestc.weglas.biz.dto.EquipmentDTO;
import org.uestc.weglas.biz.dto.PageResultDTO;
import org.uestc.weglas.biz.dto.BatchUpdateEquipmentBorrowRequest;
import org.uestc.weglas.biz.dto.UpdateEquipmentBorrowRequest;
import org.uestc.weglas.biz.dto.UpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentMapper;
import org.uestc.weglas.controller.support.ControllerBizRunner;
import org.uestc.weglas.core.model.Equipment;
import org.uestc.weglas.core.model.UserContext;
import org.uestc.weglas.core.service.EquipmentBorrowService;
import org.uestc.weglas.core.service.EquipmentChangeLogService;
import org.uestc.weglas.core.service.EquipmentService;
import org.uestc.weglas.core.util.UserContextHolder;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentChangeLogService changeLogService;

    @Autowired
    private EquipmentBorrowService equipmentBorrowService;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @GetMapping("/equipment/by-code/{assetCode}.json")
    public BaseResult<EquipmentDTO> getByAssetCode(@PathVariable("assetCode") String assetCode) {
        final EquipmentService eqSvc = equipmentService;
        final EquipmentBorrowService borrowSvc = equipmentBorrowService;
        final EquipmentMapper mapper = equipmentMapper;
        return ControllerBizRunner.run(result -> {
            if (UserContextHolder.isStaff()) {
                EquipmentEntity entity = mapper.selectByAssetCode(assetCode.trim());
                result.setData(borrowSvc.buildStaffEquipmentDto(entity));
            } else {
                Equipment equipment = eqSvc.queryByAssetCode(assetCode);
                result.setData(EquipmentDTOConverter.toDTO(equipment));
            }
        });
    }

    @PostMapping("/equipment/by-code/{assetCode}/update-borrow.json")
    public BaseResult<EquipmentDTO> updateBorrow(@PathVariable("assetCode") String assetCode,
                                                 @RequestBody UpdateEquipmentBorrowRequest request) {
        final EquipmentBorrowService borrowSvc = equipmentBorrowService;
        return ControllerBizRunner.run(result ->
                result.setData(borrowSvc.updateEquipmentBorrow(assetCode, request)));
    }

    @PostMapping("/equipment/batch-update-borrow.json")
    public BaseResult<BatchUpdateResultDTO> batchUpdateBorrow(@RequestBody BatchUpdateEquipmentBorrowRequest request) {
        final EquipmentBorrowService borrowSvc = equipmentBorrowService;
        return ControllerBizRunner.run(result ->
                result.setData(borrowSvc.batchUpdateEquipmentBorrow(request)));
    }

    @GetMapping("/rooms/{roomCode}/equipment.json")
    public BaseResult<List<EquipmentDTO>> listByRoom(@PathVariable("roomCode") String roomCode,
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "usageStatus", required = false) String usageStatus,
                                                     @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
                                                     @RequestParam(value = "borrowType", required = false) String borrowType) {
        final EquipmentService eqSvc = equipmentService;
        return ControllerBizRunner.run(result -> {
            List<Equipment> list = eqSvc.listByRoomCode(roomCode, keyword, usageStatus, sortOrder, borrowType);
            List<EquipmentDTO> dtoList = new ArrayList<>();
            for (Equipment equipment : list) {
                dtoList.add(EquipmentDTOConverter.toDTO(equipment));
            }
            result.setData(dtoList);
        });
    }

    @GetMapping("/equipment/search.json")
    public BaseResult<List<EquipmentDTO>> search(@RequestParam("keyword") String keyword) {
        final EquipmentService eqSvc = equipmentService;
        return ControllerBizRunner.run(result -> {
            List<Equipment> list = eqSvc.search(keyword);
            List<EquipmentDTO> dtoList = new ArrayList<>();
            for (Equipment equipment : list) {
                dtoList.add(EquipmentDTOConverter.toDTO(equipment));
            }
            result.setData(dtoList);
        });
    }

    @PostMapping("/equipment/by-code/{assetCode}/update-status.json")
    public BaseResult<EquipmentDTO> updateStatus(@PathVariable("assetCode") String assetCode,
                                                 @RequestBody UpdateEquipmentStatusRequest request) {
        final EquipmentService eqSvc = equipmentService;
        return ControllerBizRunner.runWithCheck(
                () -> RequestValidator.valid(request),
                result -> {
                    UserContext ctx = UserContextHolder.get();
                    String operatorId = ctx != null ? ctx.getUserId() : null;
                    String operatorName = ctx != null ? ctx.getName() : null;
                    if (StringUtils.isBlank(request.getSource())) {
                        request.setSource("SCAN");
                    }
                    Equipment equipment = eqSvc.updateStatus(assetCode, request, operatorId, operatorName);
                    result.setData(EquipmentDTOConverter.toDTO(equipment));
                });
    }

    @PostMapping("/equipment/batch-update-status.json")
    public BaseResult<BatchUpdateResultDTO> batchUpdateStatus(@RequestBody BatchUpdateEquipmentStatusRequest request) {
        final EquipmentService eqSvc = equipmentService;
        return ControllerBizRunner.runWithCheck(
                () -> RequestValidator.valid(request),
                result -> {
                    UserContext ctx = UserContextHolder.get();
                    String operatorId = ctx != null ? ctx.getUserId() : null;
                    String operatorName = ctx != null ? ctx.getName() : null;
                    if (StringUtils.isBlank(request.getSource())) {
                        request.setSource("BATCH");
                    }
                    result.setData(eqSvc.batchUpdateStatus(request, operatorId, operatorName));
                });
    }

    @PostMapping("/equipment/by-code/{assetCode}/delete.json")
    public BaseResult<EquipmentDTO> delete(@PathVariable("assetCode") String assetCode,
                                           @RequestBody(required = false) DeleteEquipmentRequest request) {
        final EquipmentService eqSvc = equipmentService;
        return ControllerBizRunner.run(result -> {
            UserContext ctx = UserContextHolder.get();
            String operatorId = ctx != null ? ctx.getUserId() : null;
            String operatorName = ctx != null ? ctx.getName() : null;
            String remark = request != null ? request.getRemark() : null;
            Equipment equipment = eqSvc.delete(assetCode, remark, "DELETE", operatorId, operatorName);
            result.setData(EquipmentDTOConverter.toDTO(equipment));
        });
    }

    @PostMapping("/equipment/batch-delete.json")
    public BaseResult<BatchUpdateResultDTO> batchDelete(@RequestBody BatchDeleteEquipmentRequest request) {
        final EquipmentService eqSvc = equipmentService;
        return ControllerBizRunner.runWithCheck(
                () -> RequestValidator.valid(request),
                result -> {
                    UserContext ctx = UserContextHolder.get();
                    String operatorId = ctx != null ? ctx.getUserId() : null;
                    String operatorName = ctx != null ? ctx.getName() : null;
                    result.setData(eqSvc.batchDelete(request, operatorId, operatorName));
                });
    }

    @GetMapping("/equipment/change-logs.json")
    public BaseResult<PageResultDTO<EquipmentChangeLogDTO>> pageChangeLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "fieldName", required = false) String fieldName) {
        final EquipmentChangeLogService logSvc = changeLogService;
        return ControllerBizRunner.run(result ->
                result.setData(logSvc.pageRoomAndStatusChanges(page, pageSize, fieldName)));
    }

    @GetMapping("/equipment/{equipmentId}/changes.json")
    public BaseResult<List<EquipmentChangeLogDTO>> listChanges(@PathVariable("equipmentId") String equipmentId) {
        final EquipmentChangeLogService logSvc = changeLogService;
        return ControllerBizRunner.run(result ->
                result.setData(logSvc.listByEquipmentId(equipmentId, 50)));
    }
}
