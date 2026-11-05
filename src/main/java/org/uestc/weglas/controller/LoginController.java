package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.base.util.validator.RequestValidator;
import org.uestc.weglas.biz.dto.LoginRequest;
import org.uestc.weglas.biz.dto.LoginResponse;
import org.uestc.weglas.biz.dto.StudentLoginRequest;
import org.uestc.weglas.controller.helper.LoginSessionHelper;
import org.uestc.weglas.controller.support.ControllerBizRunner;
import org.uestc.weglas.core.model.User;
import org.uestc.weglas.core.service.SessionService;
import org.uestc.weglas.core.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Value("${app.session.ttl-seconds:259200}")
    private long sessionTtlSeconds;

    @PostMapping("/login.json")
    public BaseResult<LoginResponse> login(@RequestBody LoginRequest request,
                                           HttpServletResponse httpResponse) {
        final UserService userSvc = userService;
        final SessionService sessionSvc = sessionService;
        final long ttlSeconds = sessionTtlSeconds;
        return ControllerBizRunner.runWithCheck(
                () -> RequestValidator.valid(request),
                result -> {
                    User user = userSvc.login(request.getAccount(), request.getPassword());
                    AssertUtil.notNull(user, "用户名或密码错误");
                    result.setData(LoginSessionHelper.createLoginSession(
                            user, httpResponse, sessionSvc, ttlSeconds));
                });
    }

    @PostMapping("/student/login.json")
    public BaseResult<LoginResponse> studentLogin(@RequestBody StudentLoginRequest request,
                                                  HttpServletResponse httpResponse) {
        final UserService userSvc = userService;
        final SessionService sessionSvc = sessionService;
        final long ttlSeconds = sessionTtlSeconds;
        return ControllerBizRunner.runWithCheck(
                () -> RequestValidator.valid(request),
                result -> {
                    User user = userSvc.loginStudent(request);
                    AssertUtil.notNull(user, "学号或手机号不正确");
                    result.setData(LoginSessionHelper.createLoginSession(
                            user, httpResponse, sessionSvc, ttlSeconds));
                });
    }

    @PostMapping("/logout.json")
    public BaseResult<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        final SessionService sessionSvc = sessionService;
        return ControllerBizRunner.run(result -> {
            LoginSessionHelper.destroyLoginSession(httpRequest, httpResponse, sessionSvc);
            result.setData(null);
        });
    }
}
