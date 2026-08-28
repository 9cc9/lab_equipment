package org.uestc.weglas.base.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.uestc.weglas.base.dal.entity.RoomEntity;

import java.util.List;

@Component
@Mapper
public interface RoomMapper {

    @Select("SELECT * FROM le_room WHERE room_code = #{roomCode}")
    RoomEntity selectByRoomCode(@Param("roomCode") String roomCode);

    @Select("SELECT * FROM le_room WHERE id = #{id}")
    RoomEntity selectById(@Param("id") String id);

    @Select("SELECT * FROM le_room WHERE status = 'ENABLED' ORDER BY room_code")
    List<RoomEntity> selectAllEnabled();

    int insert(RoomEntity entity);

    int updateById(RoomEntity entity);
}
