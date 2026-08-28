package org.uestc.weglas.base.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.uestc.weglas.base.dal.entity.UserEntity;

@Component
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM le_user WHERE username = #{username}")
    UserEntity selectByUsername(@Param("username") String username);
}
