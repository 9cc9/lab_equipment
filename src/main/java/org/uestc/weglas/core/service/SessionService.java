package org.uestc.weglas.core.service;

import org.uestc.weglas.core.model.UserContext;

/**
 * Session 服务接口
 * 管理 Redis 中的用户 Session
 *
 * @author yingxian.cyx
 * @date Created in 2025-01-27
 */
public interface SessionService {

    /**
     * 创建 Session
     * 将 UserContext 存储到 Redis，并设置过期时间
     *
     * @param sessionToken Session Token（UUID）
     * @param userContext  用户上下文
     * @param ttlSeconds  过期时间（秒）
     */
    void createSession(String sessionToken, UserContext userContext, long ttlSeconds);

    /**
     * 获取 Session
     * 从 Redis 中获取 UserContext
     *
     * @param sessionToken Session Token
     * @return 用户上下文，如果不存在或已过期返回 null
     */
    UserContext getSession(String sessionToken);

    /**
     * 删除 Session
     * 从 Redis 中删除 Session
     *
     * @param sessionToken Session Token
     */
    void deleteSession(String sessionToken);

    /**
     * 检查 Session 是否存在
     *
     * @param sessionToken Session Token
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsSession(String sessionToken);
}
