package org.uestc.weglas.core.service;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uestc.weglas.base.dal.entity.UserEntity;
import org.uestc.weglas.base.dal.mapper.UserMapper;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.core.enums.Status;
import org.uestc.weglas.core.model.User;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String account, String password) {
        AssertUtil.notBlank(account);
        AssertUtil.notBlank(password);

        UserEntity entity = userMapper.selectByUsername(account.trim());
        AssertUtil.notNull(entity, "用户名或密码错误");

        Status status = Status.fromCode(entity.getStatus());
        AssertUtil.notNull(status, "账号状态异常");
        AssertUtil.isTrue(status.isEnabled(), "账号已禁用");
        AssertUtil.isTrue(BCrypt.checkpw(password, entity.getPassword()), "用户名或密码错误");

        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();
    }
}
