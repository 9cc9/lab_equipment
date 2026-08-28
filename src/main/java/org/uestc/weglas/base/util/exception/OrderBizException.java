package org.uestc.weglas.base.util.exception;

import org.uestc.weglas.core.enums.ResultEnum;

/**
 * 定义通用业务异常
 *
 * @author yingxian.cyx
 * @date Created in 2024/6/21
 */
public class OrderBizException extends RuntimeException {

    private String errorCode;

    public OrderBizException() {
        super();
    }

    public OrderBizException(String message) {
        super(message);
        this.errorCode = ResultEnum.SYSTEM_EXCEPTION.getCode();
    }

    public OrderBizException(ResultEnum resultCode) {
        super(resultCode.getCode());
        this.errorCode = resultCode.getCode();
    }


    public OrderBizException(ResultEnum resultCode,String message) {
        super(message);
        this.errorCode = resultCode.getCode();
    }

    public OrderBizException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ResultEnum.SYSTEM_EXCEPTION.getCode();
    }

    public OrderBizException(Throwable cause) {
        super(cause);
        this.errorCode = ResultEnum.SYSTEM_EXCEPTION.getCode();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
