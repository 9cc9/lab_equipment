package org.uestc.weglas.core.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.uestc.weglas.core.model.UserContext;

import java.util.concurrent.TimeUnit;

/**
 * Session 服务实现
 * 使用 Redis 存储用户 Session
 *
 * @author yingxian.cyx
 * @date Created in 2025-01-27
 */
@Service
@Slf4j
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_KEY_PREFIX = "session:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void createSession(String sessionToken, UserContext userContext, long ttlSeconds) {
        String key = SESSION_KEY_PREFIX + sessionToken;
        String value = JSON.toJSONString(userContext);
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        log.debug("创建 Session: sessionToken={}, userId={}, ttl={}秒", sessionToken, userContext.getUserId(), ttlSeconds);
    }

    @Override
    public UserContext getSession(String sessionToken) {
        String key = SESSION_KEY_PREFIX + sessionToken;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return JSON.parseObject(value, UserContext.class);
        } catch (Exception e) {
            log.error("解析 Session 失败: sessionToken={}, error={}", sessionToken, e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteSession(String sessionToken) {
        String key = SESSION_KEY_PREFIX + sessionToken;
        redisTemplate.delete(key);
        log.debug("删除 Session: sessionToken={}", sessionToken);
    }

    @Override
    public boolean existsSession(String sessionToken) {
        String key = SESSION_KEY_PREFIX + sessionToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
