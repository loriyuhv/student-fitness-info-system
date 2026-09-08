package com.wsw.fitnesssystem.shared.context;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * 请求上下文持有者（ThreadLocal 容器）
 *
 * <p>该类负责管理当前线程的 {@link RequestContext} 对象，提供静态存取方法。
 * 它封装了 ThreadLocal 的复杂性，确保数据在当前请求线程内隔离。</p>
 *
 * <p><b>生命周期管理（极其重要）：</b>
 * <ul>
 *   <li><b>设置：</b>在 {@code JwtAuthenticationFilter} 解析 Token 成功后调用
 *   {@link #setContext(RequestContext)}。</li>
 *   <li><b>清理：</b>必须在请求结束的 {@code finally} 块中调用 {@link #clear()}，
 *   防止线程池复用导致上下文泄露或串号。</li>
 * </ul>
 *
 * <p><b>为什么与 RequestContext 分离？</b>
 * <ul>
 *   <li><b>单一职责：</b>RequestContext 只装数据，Holder 只管存储。</li>
 *   <li><b>可测试性：</b>业务逻辑可仅依赖 RequestContext 纯数据对象（构造注入），
 *   无需启动 ThreadLocal，便于单元测试。</li>
 *   <li><b>扩展性：</b>未来若需切换到 InheritableThreadLocal 或
 *   TransmittableThreadLocal，只需修改 Holder，数据模型（Context）保持不变。</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 02:04
 * @since 1.0
 */
public final class RequestContextHolder {

    /**
     * 私有构造器，防止实例化（纯静态工具类）。
     */
    private RequestContextHolder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 线程本地存储容器，保存当前线程的 RequestContext。
     * 每个线程独立，互不干扰。
     */
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前线程的请求上下文。
     *
     * <p>通常在 JWT 认证过滤器（{@code JwtAuthenticationFilter}）中调用，
     * 必须在调用 {@code filterChain.doFilter()} 之前执行。
     *
     * @param context 请求上下文对象（不可为 null）
     * @throws IllegalArgumentException 如果 context 为 null
     */
    public static void setContext(RequestContext context) {
        if (context == null) {
            throw new IllegalArgumentException("RequestContext must not be null");
        }
        CONTEXT.set(context);
    }

    /**
     * 获取当前线程的请求上下文。
     *
     * <p><b>注意：</b>该方法可能返回 null（如在白名单接口或未登录状态）。
     * 调用方需自行判空，或使用 {@link #getRequiredOperator()} 强制获取。
     *
     * @return 当前请求上下文，若未设置则返回 null
     */
    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    /**
     * 安全获取当前操作人（Operator）。
     *
     * <p>若当前线程未设置上下文，返回 null。
     * 适用于“查询接口”或“允许未登录的公共接口”。
     *
     * @return 当前操作人，若未登录则返回 null
     */
    public static Operator getOperator() {
        RequestContext ctx = CONTEXT.get();
        return ctx != null ? ctx.operator() : null;
    }

    /**
     * 强制获取当前操作人（Operator）。
     *
     * <p>若当前线程未设置上下文，或上下文中的 operator 为 null（理论上不可能），
     * 则抛出异常。适用于“必须登录才能访问的业务接口”。
     *
     * @return 当前操作人（永远不为 null）
     * @throws IllegalStateException 如果未登录或上下文未设置
     */
    public static Operator getRequiredOperator() {
        Operator operator = getOperator();
        if (operator == null) {
            throw new IllegalStateException("当前请求未登录，无法获取 Operator");
        }
        return operator;
    }

    /**
     * 安全获取当前令牌 ID（tokenId）。
     *
     * <p>若当前线程未设置上下文，或上下文中不包含 tokenId，返回 null。
     * 适用于需要日志追踪或操作审计的场景。
     *
     * @return 当前令牌 ID，若不存在则返回 null
     */
    public static String getTokenId() {
        RequestContext ctx = CONTEXT.get();
        return ctx != null ? ctx.tokenId() : null;
    }

    /**
     * 判断当前请求是否已登录（即是否携带有效的 RequestContext）。
     *
     * @return true 表示已设置上下文（通常意味着已登录），false 表示未登录
     */
    public static boolean isLogin() {
        return CONTEXT.get() != null;
    }

    /**
     * 强制清理当前线程的请求上下文。
     *
     * <p><b>⚠️ 必须调用：</b>该方法的调用位置极其关键。
     * 必须在 {@code JwtAuthenticationFilter} 的 {@code finally} 块中调用，
     * 确保无论是正常放行还是抛出异常，都能执行清理。
     *
     * <p>如果不清理，Tomcat 线程池复用时会携带上一次请求的用户数据，
     * 导致严重的“串号”安全漏洞（A 用户的请求落到了 B 用户的线程）。
     */
    public static void clear() {
        CONTEXT.remove();
    }

}
