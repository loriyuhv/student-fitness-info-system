package com.wsw.fitnesssystem.shared.util;

import java.util.regex.Pattern;

/**
 * 通用校验工具类
 *
 * @author loriyuhv
 * @version 1.0 2026/9/7 04:18
 * @since 1.0
 */
public class ValidationUtils {

    // 符合 RFC 5322 实用子集的邮箱正则，覆盖 99% 真实场景
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /** 手机号正则（中国大陆） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private ValidationUtils() {
    }

    /**
     * 校验邮箱格式是否合法
     *
     * @param email 待校验邮箱
     * @return true-合法，false-不合法（null 或空串也返回 false）
     */
    public static boolean isEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 校验手机号是否合法
     *
     * @param phone 待校验手机号
     * @return true-合法，false-不合法（null 或空串也返回 false）
     */
    public static boolean isPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }

        return PHONE_PATTERN.matcher(phone).matches();
    }

}
