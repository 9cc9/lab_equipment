package org.uestc.weglas.core.service;

import cn.hutool.crypto.digest.BCrypt;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uestc.weglas.base.dal.entity.UserEntity;
import org.uestc.weglas.base.dal.mapper.UserMapper;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.biz.dto.StudentLoginRequest;
import org.uestc.weglas.core.enums.Status;
import org.uestc.weglas.core.enums.UserType;
import org.uestc.weglas.core.model.User;
import org.uestc.weglas.core.util.IdGenerator;

import java.util.Date;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ASSISTANT_DEFAULT_PASSWORD = "123456";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern STUDENT_NO_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");

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
        UserEntity entity = userMapper.selectByUsername(username);
        if (entity != null && UserType.STUDENT.getCode().equals(entity.getUserType())) {
            AssertUtil.isTrue(false, "请使用同学借用入口登录");
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(username)
                || (entity != null && UserType.ADMIN.getCode().equals(entity.getUserType()))) {
            return loginAdmin(username, password);
        }
        return loginAssistant(username, password);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User loginStudent(StudentLoginRequest request) {
        String studentNo = request.getStudentNo().trim();
        String phone = request.getPhone().trim();
        AssertUtil.isTrue(STUDENT_NO_PATTERN.matcher(studentNo).matches(), "学号或手机号不正确");
        AssertUtil.isTrue(PHONE_PATTERN.matcher(phone).matches(), "学号或手机号不正确");

        UserEntity entity = userMapper.selectByStudentNo(studentNo);
        if (entity == null) {
            entity = userMapper.selectByUsername(studentNo);
        }
        if (entity != null && !UserType.STUDENT.getCode().equals(resolveUserType(entity))) {
            AssertUtil.isTrue(false, "该学号不可用于同学登录");
        }
        if (entity == null) {
            AssertUtil.notBlank(request.getName(), "首次登录请填写姓名");
            entity = createStudentUser(studentNo, phone, request.getName().trim());
        } else {
            assertEnabled(entity);
            AssertUtil.isTrue(phone.equals(entity.getPhone()) || BCrypt.checkpw(phone, entity.getPassword()),
                    "学号或手机号不正确");
        }
        return toUser(entity);
    }

    private User loginAdmin(String username, String password) {
        UserEntity entity = userMapper.selectByUsername(username);
        AssertUtil.notNull(entity, "用户名或密码错误");
        assertEnabled(entity);
        AssertUtil.isTrue(BCrypt.checkpw(password, entity.getPassword()), "用户名或密码错误");
        return toUser(entity);
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
        return toUser(entity);
    }

    private UserEntity createAssistantUser(String name) {
        Date now = new Date();
        UserEntity entity = new UserEntity();
        entity.setId(idGenerator.generate(IdGenerator.EntityType.USER));
        entity.setUsername(name);
        entity.setName(name);
        entity.setPassword(BCrypt.hashpw(ASSISTANT_DEFAULT_PASSWORD));
        entity.setStatus(Status.ENABLED.getCode());
        entity.setUserType(UserType.ASSISTANT.getCode());
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

    private UserEntity createStudentUser(String studentNo, String phone, String name) {
        Date now = new Date();
        UserEntity entity = new UserEntity();
        entity.setId(idGenerator.generate(IdGenerator.EntityType.USER));
        entity.setUsername(studentNo);
        entity.setName(name);
        entity.setPassword(BCrypt.hashpw(phone));
        entity.setStatus(Status.ENABLED.getCode());
        entity.setUserType(UserType.STUDENT.getCode());
        entity.setStudentNo(studentNo);
        entity.setPhone(phone);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        userMapper.insert(entity);
        return entity;
    }

    private void assertEnabled(UserEntity entity) {
        Status status = Status.fromCode(entity.getStatus());
        AssertUtil.notNull(status, "账号状态异常");
        AssertUtil.isTrue(status.isEnabled(), "账号已禁用");
    }

    private String resolveUserType(UserEntity entity) {
        if (StringUtils.isNotBlank(entity.getUserType())) {
            return entity.getUserType();
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(entity.getUsername())) {
            return UserType.ADMIN.getCode();
        }
        return UserType.ASSISTANT.getCode();
    }

    private User toUser(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .name(entity.getName())
                .status(entity.getStatus())
                .userType(resolveUserType(entity))
                .studentNo(entity.getStudentNo())
                .phone(entity.getPhone())
                .build();
    }
}
