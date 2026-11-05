package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.biz.dto.BorrowApplicationDTO;
import org.uestc.weglas.biz.dto.BorrowRemarkRequest;
import org.uestc.weglas.biz.dto.PageResultDTO;
import org.uestc.weglas.controller.support.ControllerBizRunner;
import org.uestc.weglas.core.service.EquipmentBorrowService;

@RestController
public class AdminBorrowController {

    @Autowired
    private EquipmentBorrowService equipmentBorrowService;

    @GetMapping("/admin/borrow/applications.json")
    public BaseResult<PageResultDTO<BorrowApplicationDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "borrowType", required = false) String borrowType,
            @RequestParam(value = "assetCode", required = false) String assetCode,
            @RequestParam(value = "studentNo", required = false) String studentNo,
            @RequestParam(value = "overdueOnly", required = false) Boolean overdueOnly) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result -> result.setData(
                svc.adminListApplications(page, pageSize, status, borrowType, assetCode, studentNo, overdueOnly)));
    }

    @GetMapping("/admin/borrow/due-warnings.json")
    public BaseResult<PageResultDTO<BorrowApplicationDTO>> dueWarnings(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result -> result.setData(svc.adminListDueWarnings(page, pageSize)));
    }

    @PostMapping("/admin/borrow/applications/{id}/approve.json")
    public BaseResult<BorrowApplicationDTO> approve(@PathVariable("id") String id,
                                                    @RequestBody(required = false) BorrowRemarkRequest request) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        final String remark = request != null ? request.getRemark() : null;
        return ControllerBizRunner.run(result -> result.setData(svc.approve(id, remark)));
    }

    @PostMapping("/admin/borrow/applications/{id}/reject.json")
    public BaseResult<BorrowApplicationDTO> reject(@PathVariable("id") String id,
                                                   @RequestBody BorrowRemarkRequest request) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        final String remark = request != null ? request.getRemark() : null;
        return ControllerBizRunner.run(result -> result.setData(svc.reject(id, remark)));
    }

    @PostMapping("/admin/borrow/applications/{id}/return.json")
    public BaseResult<BorrowApplicationDTO> confirmReturn(@PathVariable("id") String id,
                                                          @RequestBody(required = false) BorrowRemarkRequest request) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        final String remark = request != null ? request.getRemark() : null;
        return ControllerBizRunner.run(result -> result.setData(svc.confirmReturn(id, remark)));
    }
}
