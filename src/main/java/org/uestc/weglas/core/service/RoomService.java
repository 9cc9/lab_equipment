package org.uestc.weglas.core.service;

import org.uestc.weglas.core.model.Room;

import java.util.List;

public interface RoomService {
    List<Room> listRoomsWithCount();

    Room getOrCreateByRoomCode(String roomCode, String building);
}
