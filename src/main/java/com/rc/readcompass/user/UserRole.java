package com.rc.readcompass.user;


public enum UserRole {
    ADMIN,
    USER;

    private static final String PREFIX = "ROLE_";

    public String authority() {
        return PREFIX + name();
    }

    /**
     * "ROLE_ADMIN" 같은 authority 문자열을 UserRole로 되돌린다.
     * null이거나 알 수 없는 값이면 USER로 안전하게 처리한다.
     */
    public static UserRole fromAuthority(String authority) {
        if (authority == null) {
            return USER;
        }
        String normalized = authority.startsWith(PREFIX) ? authority.substring(PREFIX.length()) : authority;
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}
