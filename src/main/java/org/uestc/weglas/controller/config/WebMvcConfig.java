package org.uestc.weglas.controller.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.uestc.weglas.controller.interceptor.AuthInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${app.session.exclude-paths:}")
    private String excludePathsConfig;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String corsAllowedOrigins;

    @Value("${app.cors.allow-private-network:false}")
    private boolean allowPrivateNetwork;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowPrivateNetwork) {
            // 由 DevLanCorsFilter 统一处理（支持局域网 IP:5174）
            return;
        }
        List<String> origins = new ArrayList<>();
        for (String origin : corsAllowedOrigins.split(",")) {
            String trimmed = origin.trim();
            if (StringUtils.hasText(trimmed)) {
                origins.add(trimmed);
            }
        }
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        String cleanPath = resourcePath;
                        int queryIndex = cleanPath.indexOf('?');
                        if (queryIndex >= 0) {
                            cleanPath = cleanPath.substring(0, queryIndex);
                        }
                        if (cleanPath.endsWith(".json")) {
                            return null;
                        }
                        Resource requestedResource = location.createRelative(cleanPath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        return location.createRelative("index.html");
                    }
                });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> authExcludePatterns = new ArrayList<>();
        if (StringUtils.hasText(excludePathsConfig)) {
            for (String path : excludePathsConfig.split(",")) {
                String trimmedPath = path.trim();
                if (StringUtils.hasText(trimmedPath)) {
                    authExcludePatterns.add(trimmedPath);
                }
            }
        }
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**/*.json")
                .excludePathPatterns(authExcludePatterns.toArray(new String[0]));
    }
}
