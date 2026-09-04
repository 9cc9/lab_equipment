package org.uestc.weglas.core.util;

import org.uestc.weglas.core.model.UserContext;

/**
 * 用户上下文持有者
 * 使用 ThreadLocal 存储当前请求的用户上下文信息
 * 在请求结束时需要调用 clear() 清除，避免内存泄漏
 *
 * @author yingxian.cyx
 * @date Created in 2025-01-27
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置用户上下文
     *
     * @param userContext 用户上下文
     */
    public static void set(UserContext userContext) {
        CONTEXT_HOLDER.set(userContext);
    }

    /**
     * 获取用户上下文
     *
     * @return 用户上下文，如果不存在返回 null
     */
    public static UserContext get() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID，如果不存在返回 null
     */
    public static String getUserId() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取手机号
     *
     * @return 手机号，如果不存在返回 null
     */
    public static String getPhone() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getPhone() : null;
    }

    /**
     * 获取用户姓名
     *
     * @return 用户姓名，如果不存在返回 null
     */
    public static String getName() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getName() : null;
    }

    /**
     * 当前用户是否为 Guest 匿名用户
     */
    public static boolean isGuest() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null && "GUEST".equals(context.getUserType());
    }

    /**
     * 当前用户是否为管理员
     */
    public static boolean isAdmin() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null && "ADMIN".equals(context.getUserType());
    }

    /**
     * 清除用户上下文
     * 必须在请求结束时调用，避免内存泄漏
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
