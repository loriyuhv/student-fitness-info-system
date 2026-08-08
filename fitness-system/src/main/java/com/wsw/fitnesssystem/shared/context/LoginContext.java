package com.wsw.fitnesssystem.shared.context;

public class LoginContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> CAMPUS_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> TOKEN_ID_HOLDER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setCampusId(Long campusId) {
        CAMPUS_ID_HOLDER.set(campusId);
    }

    public static Long getCampusId() {
        return CAMPUS_ID_HOLDER.get();
    }

    public static void setJti(String jti) {
        TOKEN_ID_HOLDER.set(jti);
    }

    public static String getJti() {
        return TOKEN_ID_HOLDER.get();
    }

    public static boolean isLogin() {
        return getUserId() != null;
    }

    /**
     * 请求结束强制清理，防止线程池复用串号（重中之重）
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        CAMPUS_ID_HOLDER.remove();
        TOKEN_ID_HOLDER.remove();
    }
}
