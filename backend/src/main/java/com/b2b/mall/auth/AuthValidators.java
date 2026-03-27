package com.b2b.mall.auth;

import java.util.regex.Pattern;

public final class AuthValidators {

    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9_]{4,32}$");
    /** 至少 8 位，含大小写字母与数字（与常见商城注册一致） */
    private static final Pattern PASSWORD =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$");

    private AuthValidators() {}

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }
}
