package org.uestc.weglas.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.template.AbstractBizCallback;
import org.uestc.weglas.base.util.template.BizTemplate;
import org.uestc.weglas.base.util.validator.RequestValidator;
import org.uestc.weglas.biz.dto.BatchUpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.BatchUpdateResultDTO;
import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
import org.uestc.weglas.biz.dto.EquipmentDTO;
import org.uestc.weglas.biz.dto.PageResultDTO;
import org.uestc.weglas.biz.dto.UpdateEquipmentStatusRequest;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;
import org.uestc.weglas.core.model.Equipment;
import org.uestc.weglas.core.model.UserContext;
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

    @GetMapping("/equipment/by-code/{assetCode}.json")
    public BaseResult<EquipmentDTO> getByAssetCode(@PathVariable("assetCode") String assetCode) {
        return BizTemplate.execute(new AbstractBizCallback<EquipmentDTO>() {
            @Override
            public void execute(BaseResult<EquipmentDTO> result) {
                Equipment equipment = equipmentService.queryByAssetCode(assetCode);
                result.setData(EquipmentDTOConverter.toDTO(equipment));
            }
        });
    }

    @GetMapping("/rooms/{roomCode}/equipment.json")
    public BaseResult<List<EquipmentDTO>> listByRoom(@PathVariable("roomCode") String roomCode,
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "usageStatus", required = false) String usageStatus,
                                                     @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder) {
        return BizTemplate.execute(new AbstractBizCallback<List<EquipmentDTO>>() {
            @Override
            public void execute(BaseResult<List<EquipmentDTO>> result) {
                List<Equipment> list = equipmentService.listByRoomCode(roomCode, keyword, usageStatus, sortOrder);
                List<EquipmentDTO> dtoList = new ArrayList<>();
                for (Equipment equipment : list) {
                    dtoList.add(EquipmentDTOConverter.toDTO(equipment));
                }
                result.setData(dtoList);
            }
        });
    }

    @GetMapping("/equipment/search.json")
    public BaseResult<List<EquipmentDTO>> search(@RequestParam("keyword") String keyword) {
        return BizTemplate.execute(new AbstractBizCallback<List<EquipmentDTO>>() {
            @Override
            public void execute(BaseResult<List<EquipmentDTO>> result) {
                List<Equipment> list = equipmentService.search(keyword);
                List<EquipmentDTO> dtoList = new ArrayList<>();
                for (Equipment equipment : list) {
                    dtoList.add(EquipmentDTOConverter.toDTO(equipment));
                }
                result.setData(dtoList);
            }
        });
    }

    @PostMapping("/equipment/by-code/{assetCode}/update-status.json")
    public BaseResult<EquipmentDTO> updateStatus(@PathVariable("assetCode") String assetCode,
                                                 @RequestBody UpdateEquipmentStatusRequest request) {
        return BizTemplate.execute(new AbstractBizCallback<EquipmentDTO>() {
            @Override
            public void checkParameter() {
                RequestValidator.valid(request);
            }

            @Override
            public void execute(BaseResult<EquipmentDTO> result) {
                UserContext ctx = UserContextHolder.get();
                String operatorId = ctx != null ? ctx.getUserId() : null;
                String operatorName = ctx != null ? ctx.getName() : null;
                if (StringUtils.isBlank(request.getSource())) {
                    request.setSource("SCAN");
                }
                Equipment equipment = equipmentService.updateStatus(assetCode, request, operatorId, operatorName);
                result.setData(EquipmentDTOConverter.toDTO(equipment));
            }
        });
    }

    @PostMapping("/equipment/batch-update-status.json")
    public BaseResult<BatchUpdateResultDTO> batchUpdateStatus(@RequestBody BatchUpdateEquipmentStatusRequest request) {
        return BizTemplate.execute(new AbstractBizCallback<BatchUpdateResultDTO>() {
            @Override
            public void checkParameter() {
                RequestValidator.valid(request);
            }

            @Override
            public void execute(BaseResult<BatchUpdateResultDTO> result) {
                UserContext ctx = UserContextHolder.get();
                String operatorId = ctx != null ? ctx.getUserId() : null;
                String operatorName = ctx != null ? ctx.getName() : null;
                if (StringUtils.isBlank(request.getSource())) {
                    request.setSource("BATCH");
                }
                result.setData(equipmentService.batchUpdateStatus(request, operatorId, operatorName));
            }
        });
    }

    @GetMapping("/equipment/change-logs.json")
    public BaseResult<PageResultDTO<EquipmentChangeLogDTO>> pageChangeLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return BizTemplate.execute(new AbstractBizCallback<PageResultDTO<EquipmentChangeLogDTO>>() {
            @Override
            public void execute(BaseResult<PageResultDTO<EquipmentChangeLogDTO>> result) {
                result.setData(changeLogService.pageRoomAndStatusChanges(page, pageSize));
            }
        });
    }

    @GetMapping("/equipment/{equipmentId}/changes.json")
    public BaseResult<List<EquipmentChangeLogDTO>> listChanges(@PathVariable("equipmentId") String equipmentId) {
        return BizTemplate.execute(new AbstractBizCallback<List<EquipmentChangeLogDTO>>() {
            @Override
            public void execute(BaseResult<List<EquipmentChangeLogDTO>> result) {
                result.setData(changeLogService.listByEquipmentId(equipmentId, 50));
            }
        });
    }
}
