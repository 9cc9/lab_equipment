package org.uestc.weglas.core.service;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uestc.weglas.base.dal.entity.UserEntity;
import org.uestc.weglas.base.dal.mapper.UserMapper;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.core.enums.Status;
import org.uestc.weglas.core.model.User;
import org.uestc.weglas.core.util.IdGenerator;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ASSISTANT_DEFAULT_PASSWORD = "123456";
    private static final String USER_TYPE_ADMIN = "ADMIN";
    private static final String USER_TYPE_ASSISTANT = "ASSISTANT";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User login(String account, String password) {
        AssertUtil.notBlank(account, "请输入用户名");
        AssertUtil.notBlank(password, "请输入密码");

        String username = account.trim();
        if (ADMIN_USERNAME.equalsIgnoreCase(username)) {
            return loginAdmin(username, password);
        }
        return loginAssistant(username, password);
    }

    private User loginAdmin(String username, String password) {
        UserEntity entity = userMapper.selectByUsername(username);
        AssertUtil.notNull(entity, "用户名或密码错误");
        assertEnabled(entity);
        AssertUtil.isTrue(BCrypt.checkpw(password, entity.getPassword()), "用户名或密码错误");
        return toUser(entity, USER_TYPE_ADMIN);
    }

    private User loginAssistant(String username, String password) {
        AssertUtil.isTrue(ASSISTANT_DEFAULT_PASSWORD.equals(password), "用户名或密码错误");

        UserEntity entity = userMapper.selectByUsername(username);
        if (entity == null) {
            entity = createAssistantUser(username);
        } else {
            assertEnabled(entity);
            AssertUtil.isTrue(BCrypt.checkpw(password, entity.getPassword()), "用户名或密码错误");
        }
        return toUser(entity, USER_TYPE_ASSISTANT);
    }

    private UserEntity createAssistantUser(String name) {
        Date now = new Date();
        UserEntity entity = new UserEntity();
        entity.setId(idGenerator.generate(IdGenerator.EntityType.USER));
        entity.setUsername(name);
        entity.setName(name);
        entity.setPassword(BCrypt.hashpw(ASSISTANT_DEFAULT_PASSWORD));
        entity.setStatus(Status.ENABLED.getCode());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        try {
            userMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            entity = userMapper.selectByUsername(name);
            AssertUtil.notNull(entity, "创建助管账号失败，请重试");
        }
        return entity;
    }

    private void assertEnabled(UserEntity entity) {
        Status status = Status.fromCode(entity.getStatus());
        AssertUtil.notNull(status, "账号状态异常");
        AssertUtil.isTrue(status.isEnabled(), "账号已禁用");
    }

    private User toUser(UserEntity entity, String userType) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .name(entity.getName())
                .status(entity.getStatus())
                .userType(userType)
                .build();
    }
}
