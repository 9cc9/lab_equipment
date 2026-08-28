package org.uestc.weglas.controller.helper;

import org.uestc.weglas.biz.dto.LoginResponse;
import org.uestc.weglas.biz.dto.UserDTO;
import org.uestc.weglas.core.model.User;
import org.uestc.weglas.core.model.UserContext;
import org.uestc.weglas.core.service.SessionService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public final class LoginSessionHelper {

    private static final String SESSION_TOKEN_COOKIE_NAME = "SESSION_TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String X_SESSION_TOKEN_HEADER = "X-Session-Token";
    private static final String BEARER_PREFIX = "Bearer ";

    private LoginSessionHelper() {
    }

    public static LoginResponse createLoginSession(User user,
                                                   HttpServletResponse response,
                                                   SessionService sessionService,
                                                   long ttlSeconds) {
        String sessionToken = UUID.randomUUID().toString();

        UserContext userContext = UserContext.builder()
                .userId(user.getId())
                .phone(user.getUsername())
                .name(user.getName())
                .userType("ADMIN")
                .build();

        sessionService.createSession(sessionToken, userContext, ttlSeconds);

        Cookie cookie = new Cookie(SESSION_TOKEN_COOKIE_NAME, sessionToken);
        cookie.setPath("/");
        cookie.setMaxAge((int) ttlSeconds);
        response.addCookie(cookie);

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .build();

        return LoginResponse.builder()
                .user(userDTO)
                .sessionToken(sessionToken)
                .build();
    }

    public static void destroyLoginSession(HttpServletRequest request,
                                           HttpServletResponse response,
                                           SessionService sessionService) {
        String sessionToken = getSessionToken(request);
        if (sessionToken != null && !sessionToken.isEmpty()) {
            sessionService.deleteSession(sessionToken);
        }
        Cookie cookie = new Cookie(SESSION_TOKEN_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public static String getSessionToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length()).trim();
        }
        String tokenHeader = request.getHeader(X_SESSION_TOKEN_HEADER);
        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            return tokenHeader.trim();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
