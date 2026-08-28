package org.uestc.weglas.core.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uestc.weglas.base.dal.entity.RoomEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentMapper;
import org.uestc.weglas.base.dal.mapper.RoomMapper;
import org.uestc.weglas.core.model.Room;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private org.uestc.weglas.core.util.IdGenerator idGenerator;

    @Override
    public List<Room> listRoomsWithCount() {
        List<RoomEntity> entities = roomMapper.selectAllEnabled();
        List<Room> rooms = new ArrayList<>();
        for (RoomEntity entity : entities) {
            rooms.add(Room.builder()
                    .id(entity.getId())
                    .roomCode(entity.getRoomCode())
                    .roomName(StringUtils.isNotBlank(entity.getRoomName()) ? entity.getRoomName() : entity.getRoomCode())
                    .building(entity.getBuilding())
                    .equipmentCount(equipmentMapper.countByRoomId(entity.getId()))
                    .build());
        }
        return rooms;
    }

    @Override
    public Room getOrCreateByRoomCode(String roomCode, String building) {
        if (StringUtils.isBlank(roomCode)) {
            return null;
        }
        RoomEntity existing = roomMapper.selectByRoomCode(roomCode.trim());
        if (existing != null) {
            return Room.builder()
                    .id(existing.getId())
                    .roomCode(existing.getRoomCode())
                    .roomName(existing.getRoomName())
                    .building(existing.getBuilding())
                    .build();
        }
        java.util.Date now = new java.util.Date();
        RoomEntity entity = new RoomEntity();
        entity.setId(idGenerator.generate(org.uestc.weglas.core.util.IdGenerator.EntityType.ROOM));
        entity.setRoomCode(roomCode.trim());
        entity.setRoomName(roomCode.trim());
        entity.setBuilding(building);
        entity.setStatus("ENABLED");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        roomMapper.insert(entity);
        return Room.builder()
                .id(entity.getId())
                .roomCode(entity.getRoomCode())
                .roomName(entity.getRoomName())
                .building(entity.getBuilding())
                .build();
    }
}
