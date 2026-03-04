package com.wsw.fitnesssystem.auth.domain.port;

/**
 * 密码加密 / 校验端口
 * <p>目的：</p>
 * <ul>
 *     <li>防止 domain 直接依赖 BCrypt / SpringSecurity</li>
 *     <li>支持未来替换加密算法</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:25
 * @since 1.0
 */
public interface PasswordEncryptor {
    /**
     * 校验明文密码与密文是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 已加密密码
     */
    boolean matches(String rawPassword, String encodedPassword);
}
