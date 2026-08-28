package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.base.util.template.AbstractBizCallback;
import org.uestc.weglas.base.util.template.BizTemplate;
import org.uestc.weglas.base.util.validator.RequestValidator;
import org.uestc.weglas.biz.dto.LoginRequest;
import org.uestc.weglas.biz.dto.LoginResponse;
import org.uestc.weglas.controller.helper.LoginSessionHelper;
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
        return BizTemplate.execute(new AbstractBizCallback<LoginResponse>() {
            @Override
            public void checkParameter() {
                RequestValidator.valid(request);
            }

            @Override
            public void execute(BaseResult<LoginResponse> result) {
                User user = userService.login(request.getAccount(), request.getPassword());
                AssertUtil.notNull(user, "用户名或密码错误");
                LoginResponse response = LoginSessionHelper.createLoginSession(
                        user, httpResponse, sessionService, sessionTtlSeconds);
                result.setData(response);
            }
        });
    }

    @PostMapping("/logout.json")
    public BaseResult<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return BizTemplate.execute(new AbstractBizCallback<Void>() {
            @Override
            public void execute(BaseResult<Void> result) {
                LoginSessionHelper.destroyLoginSession(httpRequest, httpResponse, sessionService);
                result.setData(null);
            }
        });
    }
}
