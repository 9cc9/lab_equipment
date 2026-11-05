package org.uestc.weglas.core.enums;

public enum BorrowStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED"),
    /** 同学已申请结束借用，待管理员确认 */
    RETURN_PENDING("RETURN_PENDING"),
    RETURNED("RETURNED");

    private final String code;

    BorrowStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BorrowStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BorrowStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isActive() {
        return this == PENDING || this == APPROVED || this == RETURN_PENDING;
    }
}
