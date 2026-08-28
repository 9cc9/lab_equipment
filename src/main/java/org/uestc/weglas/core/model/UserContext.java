package org.uestc.weglas.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户上下文信息
 * 存储在 Redis Session 中，用于标识当前登录用户
 *
 * @author yingxian.cyx
 * @date Created in 2025-01-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户类型（CUSTOMER/MERCHANT）
     */
    private String userType;
}
