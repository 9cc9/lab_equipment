package org.uestc.weglas.core.enums;

public enum BorrowType {
    EXTERNAL("EXTERNAL"),
    IN_LAB("IN_LAB");

    private final String code;

    BorrowType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BorrowType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BorrowType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
