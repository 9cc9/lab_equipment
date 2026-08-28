package org.uestc.weglas.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.template.AbstractBizCallback;
import org.uestc.weglas.base.util.template.BizTemplate;
import org.uestc.weglas.base.util.validator.RequestValidator;
import org.uestc.weglas.biz.dto.EquipmentChangeLogDTO;
import org.uestc.weglas.biz.dto.EquipmentDTO;
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
