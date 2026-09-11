package com.wsw.fitnesssystem.shared.data_permission;

/**
 * 数据权限上下文持有者。
 * <p>与 {@code RequestContextHolder} 分离，因为数据权限是可选的（只在数据查询时才有意义）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:14
 * @since 1.0
 */
public class DataPermissionContextHolder {

    private static final ThreadLocal<DataPermissionContext> CONTEXT = new ThreadLocal<>();

    private DataPermissionContextHolder() {}

    public static void setContext(DataPermissionContext context) {
        CONTEXT.set(context);
    }

    public static DataPermissionContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

}
