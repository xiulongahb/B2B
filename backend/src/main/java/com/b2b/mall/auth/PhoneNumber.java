package com.b2b.mall.auth;

import java.util.regex.Pattern;

public final class PhoneNumber {

    private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    private PhoneNumber() {}

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\s+", "");
    }

    public static boolean isValidCnMobile(String normalized) {
        return normalized != null && CN_MOBILE.matcher(normalized).matches();
    }
}
