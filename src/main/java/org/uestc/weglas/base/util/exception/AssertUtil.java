package org.uestc.weglas.base.util.exception;

import org.apache.commons.lang.StringUtils;
import org.uestc.weglas.core.enums.ResultEnum;

/**
 * 断言工具类
 *
 * @author yingxian.cyx
 * @date Created in 2024/6/21
 */
public class AssertUtil {

    // isTrue 方法
    public static void isTrue(boolean condition) {
        if (!condition) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL);
        }
    }

    public static void notBlank(String value) {
        if (StringUtils.isBlank(value)) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL);
        }
    }

    public static void notBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL, message);
        }
    }

    public static void notNull(Object object) {
        if (object == null) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL, message);
        }
    }

    public static void notEmpty(java.util.Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new OrderBizException(ResultEnum.PARAMETER_ILLEGAL, message);
        }
    }
}