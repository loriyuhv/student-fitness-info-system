package com.wsw.fitnesssystem.shared.context;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

public class LoginContext {
    private static final ThreadLocal<Operator> HOLDER = new ThreadLocal<>();

    public static void setOperator(Operator operator) {
        HOLDER.set(operator);
    }

    public static Operator getOperator() {
        return HOLDER.get();
    }

    public static boolean isLogin() {
        return getOperator().userId() != null && getOperator().campusId() != null;
    }

    /**
     * 请求结束强制清理，防止线程池复用串号（重中之重）
     */
    public static void clear() {
        HOLDER.remove();
    }
}
