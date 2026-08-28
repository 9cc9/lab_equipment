package org.uestc.weglas.base.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.uestc.weglas.base.dal.entity.EquipmentChangeLogEntity;

import java.util.List;

@Component
@Mapper
public interface EquipmentChangeLogMapper {

    int insert(EquipmentChangeLogEntity entity);

    @Select("SELECT * FROM le_equipment_change_log WHERE equipment_id = #{equipmentId} ORDER BY created_at DESC LIMIT #{limit}")
    List<EquipmentChangeLogEntity> selectByEquipmentId(@Param("equipmentId") String equipmentId,
                                                       @Param("limit") int limit);
}
