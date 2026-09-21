package com.wsw.fitnesssystem.iam.authentication.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性（基础设施层配置）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>绑定 {@code iam.authn.jwt.*} 下的所有 JWT 相关配置项</li>
 *   <li>提供访问令牌 / 刷新令牌的密钥字符串、签发者、受众、有效期等参数</li>
 *   <li>不负责 {@link javax.crypto.SecretKey} Bean 的创建，密钥 Bean 由
 *       {@link JwtKeyConfiguration} 统一生产</li>
 * </ul>
 *
 * <p><b>时间单位约定：</b>
 * <ul>
 *   <li>{@link #expire} 与 {@link #refreshExpire} 单位均为 <b>秒</b>，与 YAML 配置保持一致</li>
 *   <li>需要毫秒时通过 {@link #getExpireMillis()} / {@link #getRefreshExpireMillis()} 派生获取，
 *       避免在业务代码里分散地做 {@code * 1000L} 转换</li>
 * </ul>
 *
 * <p><b>默认值兜底策略：</b>
 * 所有非敏感字段都提供默认值，YAML 未显式配置时使用默认值；
 * 敏感字段（accessSecret / refreshSecret）不提供默认值，缺失时由
 * {@link JwtKeyConfiguration} 在启动阶段直接抛出异常，做到 fail-fast。</p>
 *
 * <p><b>配置示例：</b>
 * <pre>{@code
 * iam:
 *   authn:
 *     jwt:
 *       access-secret: ${JWT_ACCESS_SECRET}
 *       refresh-secret: ${JWT_REFRESH_SECRET}
 *       min-length: 32
 *       expire: 900
 *       refresh-expire: 604800
 *       issuer: "student-fitness"
 *       audience: "web-client"
 * }</pre></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 11:05
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "iam.authn.jwt")
public class JwtProperties {

    /**
     * 访问令牌（Access Token）签名密钥（HMAC-SHA256）
     * <p>敏感配置，通过环境变量注入；不提供默认值。</p>
     */
    private String accessSecret;

    /**
     * 刷新令牌（Refresh Token）签名密钥（HMAC-SHA256）
     * <p>敏感配置，通过环境变量注入；不提供默认值。</p>
     */
    private String refreshSecret;

    /**
     * JWT 签发者标识（iss claim）
     * <p>必须与令牌中的 iss 声明匹配，用于跨系统身份隔离。</p>
     */
    private String issuer = "system";

    /**
     * JWT 受众标识（aud claim）
     * <p>标识令牌的合法消费方，用于防止令牌被非授权客户端使用。</p>
     */
    private String audience;

    /**
     * JWT 签名密钥最小长度（字节）
     * <p>HMAC-SHA256 要求密钥长度不低于 32 字节；启动阶段校验，不满足则拒绝启动。</p>
     */
    private int minLength = 32;

    /**
     * 访问令牌有效期，单位：秒
     * <p>默认 900 秒（15 分钟）；需要毫秒值时请使用 {@link #getExpireMillis()}。</p>
     */
    private long expire = 900;

    /**
     * 刷新令牌有效期，单位：秒
     * <p>默认 604800 秒（7 天）；必须大于 {@link #expire}；
     * 需要毫秒值时请使用 {@link #getRefreshExpireMillis()}。</p>
     */
    private long refreshExpire = 604800;

    /**
     * 获取访问令牌有效期（毫秒）
     * <p>派生方法：由 {@link #expire}（秒）乘以 1000 得到，供 JWT 签发时的
     * {@code expiration} 使用，避免调用方各自做单位转换。</p>
     *
     * @return 访问令牌有效期，单位：毫秒
     */
    public long getExpireMillis() {
        return expire * 1000L;
    }

    /**
     * 获取刷新令牌有效期（毫秒）
     * <p>派生方法：由 {@link #refreshExpire}（秒）乘以 1000 得到。</p>
     *
     * @return 刷新令牌有效期，单位：毫秒
     */
    public long getRefreshExpireMillis() {
        return refreshExpire * 1000L;
    }

}
