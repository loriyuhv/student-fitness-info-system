package com.wsw.fitnesssystem.shared.context;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * 请求上下文数据载体（不可变对象）
 * <p>该类封装了单次 HTTP 请求生命周期内的核心身份与技术元数据。它作为
 * {@link RequestContextHolder} 的数据单元，存储在 ThreadLocal 中。</p>
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li><b>职责单一：</b>只负责“承载数据”，不包含任何存储或获取逻辑。</li>
 *   <li><b>不可变性：</b>使用 Record 保证线程安全，所有字段一经创建不可修改。</li>
 *   <li><b>业务与技术隔离：</b>{@link Operator} 代表“谁在操作”（业务身份），
 *   {@code tokenId} 和 {@code deviceId} 代表“用什么凭证”（技术元数据）。</li>
 * </ul>
 *
 * <p><b>使用约束：</b>
 * <ul>
 *   <li>operator 字段永远不为 null（未登录场景不应当创建该上下文）。</li>
 *   <li>tokenId 在白名单接口或内部定时任务中可能为 null，调用方需做好判空。</li>
 *   <li>deviceId 仅在多端管控场景下使用，未提供时可传 null。</li>
 * </ul>
 *
 * @param operator 当前操作人（业务身份标识）。包含 userId, campusId, username, userType，永远不为 null。
 * @param tokenId 当前访问令牌 ID（jti，即 JWT ID）。用于吊销、黑名单、审计追踪。白名单接口或系统内部调用时可能为 null。
 * @param deviceId 当前设备 ID（用于多端登录控制与设备绑定）。若请求未携带设备信息，则为 null。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 02:02
 * @since 1.0
 */
public record RequestContext(Operator operator, String tokenId, String deviceId) {

    /**
     * 紧凑构造器：强制校验 Operator 不为空。
     * 保证所有 RequestContext 实例都携带有效的业务身份。
     *
     * @param operator 业务身份标识
     * @param tokenId 当前访问令牌 ID
     * @param deviceId 当前设备 ID
     */
    public RequestContext {
        if (operator == null) {
            throw new IllegalArgumentException("Operator must not be null in RequestContext");
        }
    }

    /**
     * 便捷工厂方法：仅提供 Operator（适用于只需身份，不关心凭证的查询接口）。
     *
     * @param operator 操作人（不可为 null）
     * @return 完整的 RequestContext 实例（tokenId 和 deviceId 置为 null）
     */
    public static RequestContext of(Operator operator) {
        return new RequestContext(operator, null, null);
    }

    /**
     * 便捷工厂方法：提供 Operator 和 tokenId（适用于需要吊销凭证的认证接口）。
     *
     * @param operator 操作人（不可为 null）
     * @param tokenId  访问令牌 ID（可为 null）
     * @return 完整的 RequestContext 实例（deviceId 置为 null）
     */
    public static RequestContext of(Operator operator, String tokenId) {
        return new RequestContext(operator, tokenId, null);
    }

    /**
     * 便捷工厂方法：提供完整信息（适用于登录或刷新等需要完整上下文的场景）。
     *
     * @param operator 操作人（不可为 null）
     * @param tokenId  访问令牌 ID（可为 null）
     * @param deviceId 设备 ID（可为 null）
     * @return 完整的 RequestContext 实例
     */
    public static RequestContext of(Operator operator, String tokenId, String deviceId) {
        return new RequestContext(operator, tokenId, deviceId);
    }

}
