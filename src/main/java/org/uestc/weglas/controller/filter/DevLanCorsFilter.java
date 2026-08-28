package org.uestc.weglas.controller.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 开发环境：允许局域网 IP 通过 Vite (5174) 跨域访问 API。
 * Spring Boot 2.1 的 CorsRegistry 不支持 origin 通配，用 Filter 补充私网段。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.cors.allow-private-network", havingValue = "true", matchIfMissing = false)
public class DevLanCorsFilter extends OncePerRequestFilter {

    private static final Pattern PRIVATE_NETWORK_ORIGIN = Pattern.compile(
            "https?://("
                    + "localhost|127\\.0\\.0\\.1"
                    + "|192\\.168\\.\\d{1,3}\\.\\d{1,3}"
                    + "|10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
                    + "|172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3}"
                    + "):5174"
    );

    private final Set<String> staticOrigins = new HashSet<>();

    public DevLanCorsFilter(@Value("${app.cors.allowed-origins:}") String corsAllowedOrigins) {
        if (StringUtils.hasText(corsAllowedOrigins)) {
            Arrays.stream(corsAllowedOrigins.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(staticOrigins::add);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (isAllowedOrigin(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            // credentials 模式下 Allow-Headers 不能用 *，需显式列出或回显预检请求头
            String requestHeaders = request.getHeader("Access-Control-Request-Headers");
            if (StringUtils.hasText(requestHeaders)) {
                response.setHeader("Access-Control-Allow-Headers", requestHeaders);
            } else {
                response.setHeader("Access-Control-Allow-Headers",
                        "Content-Type, Authorization, X-Session-Token, Accept, X-Requested-With");
            }
            response.setHeader("Access-Control-Max-Age", "3600");
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOrigin(String origin) {
        if (!StringUtils.hasText(origin)) {
            return false;
        }
        if (staticOrigins.contains(origin)) {
            return true;
        }
        return PRIVATE_NETWORK_ORIGIN.matcher(origin).matches();
    }
}
