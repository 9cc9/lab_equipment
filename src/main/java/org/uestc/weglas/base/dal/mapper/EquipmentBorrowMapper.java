package org.uestc.weglas.base.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.uestc.weglas.base.dal.entity.EquipmentBorrowEntity;

import java.util.List;

@Component
@Mapper
public interface EquipmentBorrowMapper {

    int insert(EquipmentBorrowEntity entity);

    int updateById(EquipmentBorrowEntity entity);

    EquipmentBorrowEntity selectById(@Param("id") String id);

    EquipmentBorrowEntity selectActiveExternal(@Param("equipmentId") String equipmentId);

    EquipmentBorrowEntity selectActiveExternalForUpdate(@Param("equipmentId") String equipmentId);

    EquipmentBorrowEntity selectActiveInLabByStudentForUpdate(@Param("equipmentId") String equipmentId,
                                                              @Param("studentNo") String studentNo);

    List<EquipmentBorrowEntity> selectApprovedByEquipmentId(@Param("equipmentId") String equipmentId);

    int countApprovedInLabByEquipmentId(@Param("equipmentId") String equipmentId);

    List<EquipmentBorrowEntity> selectByStudentNo(@Param("studentNo") String studentNo,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    long countByStudentNo(@Param("studentNo") String studentNo);

    List<EquipmentBorrowEntity> selectAdminPage(@Param("status") String status,
                                                @Param("borrowType") String borrowType,
                                                @Param("assetCode") String assetCode,
                                                @Param("studentNo") String studentNo,
                                                @Param("overdueOnly") Boolean overdueOnly,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);

    long countAdminPage(@Param("status") String status,
                        @Param("borrowType") String borrowType,
                        @Param("assetCode") String assetCode,
                        @Param("studentNo") String studentNo,
                        @Param("overdueOnly") Boolean overdueOnly);

    int countActiveByEquipmentId(@Param("equipmentId") String equipmentId);

    List<EquipmentBorrowEntity> selectDueWarningPage(@Param("offset") int offset,
                                                     @Param("limit") int limit);

    long countDueWarning();
}
