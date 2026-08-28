package org.uestc.weglas.base.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;

import java.util.List;

@Component
@Mapper
public interface EquipmentMapper {

    @Select("SELECT * FROM le_equipment WHERE asset_code = #{assetCode}")
    EquipmentEntity selectByAssetCode(@Param("assetCode") String assetCode);

    @Select("SELECT * FROM le_equipment WHERE id = #{id}")
    EquipmentEntity selectById(@Param("id") String id);

    List<EquipmentEntity> selectByRoomId(@Param("roomId") String roomId,
                                         @Param("keyword") String keyword,
                                         @Param("usageStatus") String usageStatus,
                                         @Param("sortOrder") String sortOrder);

    List<EquipmentEntity> search(@Param("keyword") String keyword);

    int insert(EquipmentEntity entity);

    int updateById(EquipmentEntity entity);

    @Select("SELECT COUNT(1) FROM le_equipment WHERE room_id = #{roomId}")
    int countByRoomId(@Param("roomId") String roomId);

    @Select("SELECT COUNT(1) FROM le_equipment WHERE room_id = #{roomId} AND usage_status = #{usageStatus}")
    int countByRoomIdAndUsageStatus(@Param("roomId") String roomId, @Param("usageStatus") String usageStatus);
}
