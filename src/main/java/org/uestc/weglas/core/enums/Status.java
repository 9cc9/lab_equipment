package org.uestc.weglas.core.enums;

/**
 * 状态枚举
 * 统一所有核心表的状态值
 *
 * @author yingxian.cyx
 * @date Created in 2025-01-24
 */
public enum Status {

    /**
     * 待审核状态（商户注册后的初始状态，审核通过前不可登录）
     */
    PENDING("PENDING", "待审核"),

    /**
     * 启用状态
     */
    ENABLED("ENABLED", "启用"),

    /**
     * 禁用状态
     */
    DISABLED("DISABLED", "禁用"),

    /**
     * 已删除状态（软删除）
     */
    DELETED("DELETED", "删除");

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    Status(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取状态代码
     *
     * @return 状态代码（如：PENDING, ENABLED, DISABLED, DELETED）
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述（如：待审核, 启用, 禁用, 删除）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取状态枚举
     *
     * @param code 状态代码
     * @return 状态枚举，如果不存在则返回null
     */
    public static Status fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (Status status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为待审核状态
     *
     * @return true表示是待审核状态
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 判断是否为启用状态
     *
     * @return true表示是启用状态
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断是否为禁用状态
     *
     * @return true表示是禁用状态
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

    /**
     * 判断是否为已删除状态
     *
     * @return true表示是已删除状态
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * 判断是否为有效状态（启用状态）
     *
     * @return true表示是有效状态
     */
    public boolean isValid() {
        return this == ENABLED;
    }
}

