package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.template.AbstractBizCallback;
import org.uestc.weglas.base.util.template.BizTemplate;
import org.uestc.weglas.biz.dto.RoomDTO;
import org.uestc.weglas.biz.dto.converter.EquipmentDTOConverter;
import org.uestc.weglas.core.model.Room;
import org.uestc.weglas.core.service.RoomService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/rooms.json")
    public BaseResult<List<RoomDTO>> listRooms() {
        return BizTemplate.execute(new AbstractBizCallback<List<RoomDTO>>() {
            @Override
            public void execute(BaseResult<List<RoomDTO>> result) {
                List<Room> rooms = roomService.listRoomsWithCount();
                List<RoomDTO> dtoList = new ArrayList<>();
                for (Room room : rooms) {
                    dtoList.add(EquipmentDTOConverter.toDTO(room));
                }
                result.setData(dtoList);
            }
        });
    }
}
