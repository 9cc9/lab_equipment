package org.uestc.weglas.controller.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.uestc.weglas.controller.helper.LoginSessionHelper;
import org.uestc.weglas.core.model.UserContext;
import org.uestc.weglas.core.service.SessionService;
import org.uestc.weglas.core.util.UserContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * 从 Cookie 或 Header 中获取 sessionToken，验证 Session 并设置 UserContext
 * 优先级：Header Token > Cookie Token
 * 
 * 注意：排除路径的配置统一在 WebMvcConfig 中通过 excludePathPatterns 处理
 *
 * @author yingxian.cyx
 * @date Created in 2025-01-27
 */
@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许 OPTIONS 预检请求通过（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String requestPath = request.getRequestURI();

        // 获取 sessionToken（优先从 Header，再从 Cookie）
        String sessionToken = LoginSessionHelper.getSessionToken(request);

        if (sessionToken == null || sessionToken.isEmpty()) {
            // 没有 Token，返回 401
            log.debug("请求未携带 Token: path={}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Not signed in or session expired\"}");
            return false;
        }

        // 从 Redis 获取 UserContext
        UserContext userContext = sessionService.getSession(sessionToken);
        if (userContext == null) {
            // Token 不存在或已过期，返回 401
            log.debug("Session 不存在或已过期: sessionToken={}, path={}", sessionToken, requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Session expired; please sign in again\"}");
            return false;
        }

        // 设置到 ThreadLocal
        UserContextHolder.set(userContext);
        log.debug("认证成功: userId={}, path={}", userContext.getUserId(), requestPath);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除 ThreadLocal，避免内存泄漏
        UserContextHolder.clear();
    }
}
