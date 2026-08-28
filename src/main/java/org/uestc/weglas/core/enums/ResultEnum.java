package org.uestc.weglas.core.enums;

/**
 * @author yingxian.cyx
 * @date Created in 2024/10/14
 */
public enum ResultEnum {

    SUCCESS("SUCCESS", "Request succeeded"),
    PARAMETER_ILLEGAL("PARAMETER_ILLEGAL", "Validation failed"),
    INVOKE_FAIL("INVOKE_FAIL", "Request failed"),
    SYSTEM_EXCEPTION("SYSTEM_EXCEPTION", "Internal server error"),

    /** 邮箱验证码注册：验证码错误或已过期 */
    REGISTER_EMAIL_CODE_INVALID("REGISTER_EMAIL_CODE_INVALID", "Invalid or expired verification code"),
    /** 邮箱验证码注册：邮箱已被未删除用户使用 */
    REGISTER_EMAIL_ALREADY_EXISTS("REGISTER_EMAIL_ALREADY_EXISTS", "This email is already registered"),
    /** 发送注册验证码：同一邮箱触发发码间隔限制 */
    REGISTER_EMAIL_SEND_TOO_FREQUENT("REGISTER_EMAIL_SEND_TOO_FREQUENT", "Too many requests; please try again later"),
    /** Guest 用户不允许修改个人信息 */
    GUEST_OPERATION_NOT_ALLOWED("GUEST_OPERATION_NOT_ALLOWED", "Guest accounts cannot modify profile information"),
    ;

    private String code;
    private String message;

    ResultEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
