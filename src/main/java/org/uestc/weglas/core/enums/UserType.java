package org.uestc.weglas.core.enums;

public enum UserType {
    ADMIN("ADMIN"),
    ASSISTANT("ASSISTANT"),
    STUDENT("STUDENT");

    private final String code;

    UserType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UserType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
