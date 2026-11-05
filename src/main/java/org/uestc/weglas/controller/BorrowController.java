package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.validator.RequestValidator;
import org.uestc.weglas.biz.dto.*;
import org.uestc.weglas.controller.support.ControllerBizRunner;
import org.uestc.weglas.core.service.EquipmentBorrowService;
import org.uestc.weglas.core.util.UserContextHolder;

import java.util.List;

@RestController
public class BorrowController {

    @Autowired
    private EquipmentBorrowService equipmentBorrowService;

    @GetMapping("/borrow/equipment.json")
    public BaseResult<List<BorrowEquipmentDTO>> listBorrowable(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "roomCode", required = false) String roomCode,
            @RequestParam(value = "borrowType", required = false) String borrowType) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result ->
                result.setData(svc.listBorrowableEquipment(keyword, roomCode, borrowType)));
    }

    @GetMapping("/borrow/equipment/{assetCode}.json")
    public BaseResult<BorrowEquipmentDTO> borrowEquipmentDetail(@PathVariable("assetCode") String assetCode) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result ->
                result.setData(svc.getBorrowEquipmentDetail(assetCode, UserContextHolder.isStudent())));
    }

    @PostMapping("/borrow/applications.json")
    public BaseResult<BorrowApplicationDTO> submit(@RequestBody SubmitBorrowApplicationRequest request) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        final SubmitBorrowApplicationRequest body = request;
        return ControllerBizRunner.runWithCheck(
                () -> RequestValidator.valid(body),
                result -> result.setData(svc.submitApplication(body)));
    }

    @GetMapping("/borrow/applications/mine.json")
    public BaseResult<PageResultDTO<BorrowApplicationDTO>> mine(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result -> result.setData(svc.listMine(page, pageSize)));
    }

    @GetMapping("/borrow/applications/{id}.json")
    public BaseResult<BorrowApplicationDTO> get(@PathVariable("id") String id) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result -> result.setData(svc.getApplication(id)));
    }

    @PostMapping("/borrow/applications/{id}/cancel.json")
    public BaseResult<BorrowApplicationDTO> cancel(@PathVariable("id") String id) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        return ControllerBizRunner.run(result -> result.setData(svc.cancelApplication(id)));
    }

    @PostMapping("/borrow/applications/{id}/request-return.json")
    public BaseResult<BorrowApplicationDTO> requestReturn(@PathVariable("id") String id,
                                                          @RequestBody(required = false) BorrowRemarkRequest request) {
        final EquipmentBorrowService svc = equipmentBorrowService;
        final String remark = request != null ? request.getRemark() : null;
        return ControllerBizRunner.run(result -> result.setData(svc.requestReturn(id, remark)));
    }
}
