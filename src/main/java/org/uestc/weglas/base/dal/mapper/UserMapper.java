package org.uestc.weglas.base.dal.mapper;

import org.apache.ibatis.annotations.Insert;
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

    @Select("SELECT * FROM le_user WHERE student_no = #{studentNo}")
    UserEntity selectByStudentNo(@Param("studentNo") String studentNo);

    @Insert("INSERT INTO le_user (id, username, name, password, status, user_type, student_no, phone, created_at, updated_at) "
            + "VALUES (#{id}, #{username}, #{name}, #{password}, #{status}, #{userType}, #{studentNo}, #{phone}, #{createdAt}, #{updatedAt})")
    int insert(UserEntity entity);
}
